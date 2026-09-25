import { Component, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { BookingApi } from '../../core/booking-api';
import { formatCalendarDate, formatTime } from '../../core/venue-time';

/** Active reservations for one room on one venue-local day. */
@Component({
  selector: 'app-room-schedule',
  imports: [RouterLink],
  templateUrl: './room-schedule.html',
  styleUrl: './room-schedule.css',
})
export class RoomSchedule {
  readonly roomId = input.required<number>();
  /** Venue-local date, "YYYY-MM-DD". */
  readonly date = input.required<string>();
  readonly zone = input.required<string>();

  protected readonly schedule = inject(BookingApi).schedule(this.roomId, this.date);
  protected readonly formatTime = formatTime;
  protected readonly formatCalendarDate = formatCalendarDate;

  reload(): void {
    this.schedule.reload();
  }
}
