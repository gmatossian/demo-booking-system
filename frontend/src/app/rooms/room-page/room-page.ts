import { Component, computed, inject, input, linkedSignal, viewChild } from '@angular/core';
import { RouterLink } from '@angular/router';

import { toApiError } from '../../core/api-error';
import { BookingApi } from '../../core/booking-api';
import { VenueService } from '../../core/venue.service';
import { BookingForm } from '../booking-form/booking-form';
import { RoomSchedule } from '../room-schedule/room-schedule';

/** One room: its day schedule beside the booking form. The form's date drives the schedule. */
@Component({
  selector: 'app-room-page',
  imports: [RouterLink, BookingForm, RoomSchedule],
  templateUrl: './room-page.html',
  styleUrl: './room-page.css',
})
export class RoomPage {
  /** Route parameter. */
  readonly roomId = input.required<string>();

  private readonly api = inject(BookingApi);
  protected readonly venue = inject(VenueService).venue;

  protected readonly id = computed(() => Number(this.roomId()));
  protected readonly room = this.api.room(this.id);
  protected readonly people = this.api.people();
  protected readonly roomMissing = computed(() => {
    const kind = this.room.error() ? toApiError(this.room.error()).kind : undefined;
    return kind === 'not-found' || kind === 'validation';
  });

  /** Starts at the venue's today; the booking form updates it. */
  protected readonly selectedDate = linkedSignal(() =>
    this.venue.hasValue() ? this.venue.value().today : '',
  );

  private readonly schedule = viewChild(RoomSchedule);

  /** Any booking attempt may have changed the room's schedule, so show the latest. */
  protected refreshSchedule(): void {
    this.schedule()?.reload();
  }
}
