import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

import { Venue } from '../../core/api-models';
import { venueNow } from '../../core/venue-time';

export interface BookingTimes {
  date: string; // "YYYY-MM-DD"
  start: string; // "HH:mm"
  end: string;
}

export type TimeField = keyof BookingTimes;
export type TimeErrors = Partial<Record<TimeField, string>>;

/**
 * Client-side copy of the backend's time rules, for immediate feedback only.
 * Opening hours and slot boundaries are enforced by the time pickers. The
 * backend re-checks everything and is the authority.
 */
export function checkBookingTimes(
  times: BookingTimes,
  venue: Venue,
  now: { date: string; time: string },
): TimeErrors {
  const errors: TimeErrors = {};
  if (times.date && times.date < now.date) {
    errors.date = 'Choose today or a later date.';
  } else if (times.date && times.date > venue.lastBookableDate) {
    errors.date = `Reservations can be made up to ${venue.bookingHorizonDays} days ahead.`;
  }
  if (times.date === now.date && times.start && times.start < now.time) {
    errors.start = 'Start time has already passed.';
  }
  if (times.start && times.end && times.end <= times.start) {
    errors.end = 'End time must be after the start time.';
  }
  return errors;
}

/** Form-group validator exposing {@link checkBookingTimes} results as `{ times: TimeErrors }`. */
export function bookingTimesValidator(venue: () => Venue): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const { date, start, end } = group.value as BookingTimes;
    const errors = checkBookingTimes({ date, start, end }, venue(), venueNow(venue().zone));
    return Object.keys(errors).length > 0 ? { times: errors } : null;
  };
}

/** Like Validators.required, but whitespace-only text also counts as empty. */
export function requiredText(control: AbstractControl<string>): ValidationErrors | null {
  return control.value.trim() === '' ? { required: true } : null;
}
