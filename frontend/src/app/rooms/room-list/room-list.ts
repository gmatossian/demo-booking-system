import { Component, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { BookingApi } from '../../core/booking-api';

@Component({
  selector: 'app-room-list',
  imports: [RouterLink],
  templateUrl: './room-list.html',
  styleUrl: './room-list.css',
})
export class RoomList {
  /** Query parameter set after a demo data reset. */
  readonly reset = input<string>();

  protected readonly rooms = inject(BookingApi).rooms();
}
