import {
  Component,
  DestroyRef,
  OnInit,
  computed,
  inject,
  input,
  model,
  output,
  signal,
} from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import {
  FormControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subscription, finalize } from 'rxjs';

import { Person, Reservation, Room, Venue } from '../../core/api-models';
import { toApiError } from '../../core/api-error';
import { BookingApi } from '../../core/booking-api';
import { formatDate, formatTime, fromMinutes, slotTimes, toMinutes } from '../../core/venue-time';
import { TimeErrors, bookingTimesValidator, requiredText } from './booking-rules';

type FormField = 'title' | 'personId' | 'date' | 'start' | 'end';

const REQUIRED_MESSAGES: Record<FormField, string> = {
  title: 'Enter a title.',
  personId: 'Choose who the reservation is for.',
  date: 'Choose a date.',
  start: 'Choose a start time.',
  end: 'Choose an end time.',
};

/** Result of the last submission, shown below the form. */
type Outcome =
  | { kind: 'booked'; reservation: Reservation }
  | { kind: 'conflict'; conflicts: Reservation[] }
  | { kind: 'invalid'; message: string; roomError?: string }
  | { kind: 'uncertain'; message: string };

/** Result of the optional availability preview. */
type AvailabilityCheck =
  | { kind: 'checking' }
  | { kind: 'incomplete' }
  | { kind: 'free' }
  | { kind: 'busy'; conflicts: Reservation[] }
  | { kind: 'error'; message: string };

/**
 * Creates a reservation. Checking availability first is optional: the backend
 * enforces availability on every create and reports conflicts, which are shown here.
 */
@Component({
  selector: 'app-booking-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './booking-form.html',
  styleUrl: './booking-form.css',
})
export class BookingForm implements OnInit {
  readonly room = input.required<Room>();
  readonly people = input.required<Person[]>();
  readonly venue = input.required<Venue>();
  /** Venue-local date, shared with the page so the schedule follows the form. */
  readonly date = model.required<string>();
  /** Emitted after any create attempt, because the schedule may have changed. */
  readonly attempted = output<void>();

  private readonly api = inject(BookingApi);

  protected readonly form = inject(NonNullableFormBuilder).group({
    title: ['', [requiredText, Validators.maxLength(80)]],
    personId: new FormControl<number | null>(null, Validators.required),
    date: ['', Validators.required],
    start: ['', Validators.required],
    end: ['', Validators.required],
  });

  protected readonly submitting = signal(false);
  protected readonly submitAttempted = signal(false);
  protected readonly outcome = signal<Outcome | null>(null);
  protected readonly availability = signal<AvailabilityCheck | null>(null);
  private readonly serverErrors = signal<Record<string, string[]>>({});
  /** The in-flight availability check; only its answer may be shown. */
  private pendingCheck?: Subscription;

  private readonly allSlots = computed(() => {
    const { opensAt, closesAt, slotMinutes } = this.venue();
    return slotTimes(opensAt, closesAt, slotMinutes);
  });
  protected readonly startTimes = computed(() => this.allSlots().slice(0, -1));
  private readonly selectedStart = toSignal(this.form.controls.start.valueChanges, {
    initialValue: '',
  });
  protected readonly endTimes = computed(() => {
    const start = this.selectedStart();
    return this.allSlots().filter((time) => !start || time > start);
  });

  protected readonly formatTime = formatTime;
  protected readonly formatDate = formatDate;

  constructor() {
    inject(DestroyRef).onDestroy(() => this.cancelPendingCheck());
    // Any edit makes earlier results stale, including a check still in flight.
    this.form.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => {
      this.cancelPendingCheck();
      this.serverErrors.set({});
      this.availability.set(null);
      this.outcome.set(null);
    });
    this.form.controls.date.valueChanges.pipe(takeUntilDestroyed()).subscribe((date) => {
      if (date) this.date.set(date);
    });
    // Suggest a one-hour booking when the start moves past the chosen end.
    this.form.controls.start.valueChanges.pipe(takeUntilDestroyed()).subscribe((start) => {
      const end = this.form.controls.end.value;
      if (start && (!end || end <= start)) {
        const closes = toMinutes(this.venue().closesAt);
        this.form.controls.end.setValue(fromMinutes(Math.min(toMinutes(start) + 60, closes)));
      }
    });
  }

  ngOnInit(): void {
    // Added here because the time rules need the venue input, which is not set during construction.
    this.form.addValidators(bookingTimesValidator(() => this.venue()));
    this.form.controls.date.setValue(this.date(), { emitEvent: false });
  }

  protected submit(): void {
    this.submitAttempted.set(true);
    this.form.markAllAsTouched();
    if (this.form.invalid || this.submitting()) return;

    const value = this.form.getRawValue();
    this.submitting.set(true);
    this.cancelPendingCheck();
    this.availability.set(null);
    this.outcome.set(null);
    this.api
      .createReservation({
        roomId: this.room().id,
        personId: value.personId!,
        title: value.title.trim(),
        start: `${value.date}T${value.start}`,
        end: `${value.date}T${value.end}`,
      })
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: (reservation) => {
          // Keep the other fields so a similar booking is quick; clear only the title.
          this.form.controls.title.reset('', { emitEvent: false });
          this.submitAttempted.set(false);
          this.outcome.set({ kind: 'booked', reservation });
          this.attempted.emit();
        },
        error: (err) => {
          const error = toApiError(err);
          if (error.kind === 'conflict') {
            this.outcome.set({ kind: 'conflict', conflicts: error.conflicts });
          } else if (error.kind === 'validation') {
            this.serverErrors.set(error.fieldErrors);
            this.outcome.set({
              kind: 'invalid',
              message: error.message,
              roomError: error.fieldErrors['roomId']?.join(' '),
            });
          } else {
            // The request may or may not have been applied. Never retry automatically.
            this.outcome.set({ kind: 'uncertain', message: error.message });
          }
          this.attempted.emit();
        },
      });
  }

  protected checkAvailability(): void {
    this.cancelPendingCheck();
    const { date, start, end } = this.form.getRawValue();
    if (!date || !start || !end) {
      this.availability.set({ kind: 'incomplete' });
      return;
    }
    this.availability.set({ kind: 'checking' });
    this.pendingCheck = this.api
      .checkAvailability(this.room().id, `${date}T${start}`, `${date}T${end}`)
      .subscribe({
        next: (result) =>
          this.availability.set(
            result.available ? { kind: 'free' } : { kind: 'busy', conflicts: result.conflicts },
          ),
        error: (err) => {
          const error = toApiError(err);
          this.serverErrors.set(error.fieldErrors);
          this.availability.set({ kind: 'error', message: error.message });
        },
      });
  }

  /** Unsubscribing aborts the HTTP request, so a late answer can never be shown. */
  private cancelPendingCheck(): void {
    this.pendingCheck?.unsubscribe();
    this.pendingCheck = undefined;
  }

  /** Client-side and server-side messages for one field. */
  protected errorsFor(field: FormField): string[] {
    const control = this.form.controls[field];
    const messages: string[] = [];
    if (this.submitAttempted() || control.touched) {
      if (control.hasError('required')) messages.push(REQUIRED_MESSAGES[field]);
      if (control.hasError('maxlength')) messages.push('Title must be 80 characters or fewer.');
    }
    const timeErrors = this.form.errors?.['times'] as TimeErrors | undefined;
    if (field !== 'title' && field !== 'personId' && timeErrors?.[field]) {
      messages.push(timeErrors[field]);
    }
    messages.push(...(this.serverErrors()[field] ?? []));
    return [...new Set(messages)];
  }
}
