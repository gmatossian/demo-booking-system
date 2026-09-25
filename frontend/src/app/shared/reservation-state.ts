import { Component, computed, input } from '@angular/core';

import { Reservation } from '../core/api-models';

/** A badge combining stored status with the time-derived phase. */
@Component({
  selector: 'app-reservation-state',
  template: `<span class="badge" [class]="'badge ' + state().css">{{ state().label }}</span>`,
})
export class ReservationState {
  readonly reservation = input.required<Reservation>();

  protected readonly state = computed(() => {
    const { status, phase } = this.reservation();
    if (status === 'CANCELLED') return { label: 'Cancelled', css: 'badge-cancelled' };
    if (phase === 'IN_PROGRESS') return { label: 'In progress', css: 'badge-current' };
    if (phase === 'ENDED') return { label: 'Past', css: 'badge-past' };
    return { label: 'Upcoming', css: 'badge-upcoming' };
  });
}
