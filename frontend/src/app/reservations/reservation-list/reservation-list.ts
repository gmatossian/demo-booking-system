import { Component, computed, inject, input } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { ReservationView } from '../../core/api-models';
import { BookingApi } from '../../core/booking-api';
import { VenueService } from '../../core/venue.service';
import { formatDate, formatTime } from '../../core/venue-time';
import { ReservationState } from '../../shared/reservation-state';

const VIEWS: { value: ReservationView; label: string; empty: string }[] = [
  { value: 'upcoming', label: 'Upcoming', empty: 'No upcoming reservations.' },
  { value: 'past', label: 'Past', empty: 'No past reservations.' },
  { value: 'cancelled', label: 'Cancelled', empty: 'No cancelled reservations.' },
  { value: 'all', label: 'All', empty: 'No reservations yet.' },
];

@Component({
  selector: 'app-reservation-list',
  imports: [RouterLink, ReservationState],
  templateUrl: './reservation-list.html',
  styleUrl: './reservation-list.css',
})
export class ReservationList {
  /** Query parameter, e.g. ?view=past. */
  readonly view = input<string>();

  private readonly router = inject(Router);
  protected readonly venue = inject(VenueService).venue;
  protected readonly views = VIEWS;
  protected readonly selected = computed(
    () => VIEWS.find((v) => v.value === this.view()) ?? VIEWS[0],
  );
  protected readonly reservations = inject(BookingApi).reservations(() => this.selected().value);

  protected readonly formatDate = formatDate;
  protected readonly formatTime = formatTime;

  protected changeView(view: string): void {
    this.router.navigate([], { queryParams: { view } });
  }
}
