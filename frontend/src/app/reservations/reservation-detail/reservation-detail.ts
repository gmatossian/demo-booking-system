import { Component, computed, inject, input, signal, viewChild } from '@angular/core';
import { RouterLink } from '@angular/router';

import { toApiError } from '../../core/api-error';
import { BookingApi } from '../../core/booking-api';
import { VenueService } from '../../core/venue.service';
import { formatDate, formatDateTime, formatTime } from '../../core/venue-time';
import { ConfirmDialog } from '../../shared/confirm-dialog';
import { ReservationState } from '../../shared/reservation-state';

@Component({
  selector: 'app-reservation-detail',
  imports: [RouterLink, ConfirmDialog, ReservationState],
  templateUrl: './reservation-detail.html',
  styleUrl: './reservation-detail.css',
})
export class ReservationDetail {
  /** Route parameter. */
  readonly reservationId = input.required<string>();

  private readonly api = inject(BookingApi);
  protected readonly venue = inject(VenueService).venue;

  protected readonly id = computed(() => Number(this.reservationId()));
  protected readonly reservation = this.api.reservation(this.id);
  protected readonly missing = computed(() => {
    const kind = this.reservation.error() ? toApiError(this.reservation.error()).kind : undefined;
    return kind === 'not-found' || kind === 'validation';
  });

  protected readonly cancelling = signal(false);
  protected readonly cancelMessage = signal<{ kind: 'success' | 'error'; text: string } | null>(null);
  protected readonly dialog = viewChild(ConfirmDialog);

  protected readonly formatDate = formatDate;
  protected readonly formatTime = formatTime;
  protected readonly formatDateTime = formatDateTime;

  protected cancel(): void {
    this.cancelling.set(true);
    this.cancelMessage.set(null);
    this.api.cancelReservation(this.id()).subscribe({
      next: (updated) => {
        this.reservation.set(updated);
        this.cancelMessage.set({ kind: 'success', text: 'Reservation cancelled. The room is free again.' });
        this.cancelling.set(false);
      },
      error: (err) => {
        const error = toApiError(err);
        const text =
          error.kind === 'conflict' || error.kind === 'not-found'
            ? error.message
            : `We could not confirm whether the reservation was cancelled. ${error.message}`;
        this.cancelMessage.set({ kind: 'error', text });
        this.cancelling.set(false);
        // Show the reservation's current state rather than guessing.
        this.reservation.reload();
      },
    });
  }
}
