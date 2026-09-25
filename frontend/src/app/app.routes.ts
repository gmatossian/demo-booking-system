import { Routes } from '@angular/router';

import { NotFoundPage } from './not-found-page';
import { ReservationDetail } from './reservations/reservation-detail/reservation-detail';
import { ReservationList } from './reservations/reservation-list/reservation-list';
import { RoomList } from './rooms/room-list/room-list';
import { RoomPage } from './rooms/room-page/room-page';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'rooms' },
  { path: 'rooms', component: RoomList, title: 'Rooms · Room booking demo' },
  { path: 'rooms/:roomId', component: RoomPage, title: 'Book a room · Room booking demo' },
  { path: 'reservations', component: ReservationList, title: 'Reservations · Room booking demo' },
  {
    path: 'reservations/:reservationId',
    component: ReservationDetail,
    title: 'Reservation · Room booking demo',
  },
  { path: '**', component: NotFoundPage, title: 'Not found · Room booking demo' },
];
