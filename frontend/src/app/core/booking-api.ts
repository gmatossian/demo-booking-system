import { HttpClient, httpResource } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import {
  Availability,
  CreateReservationRequest,
  DemoResetSummary,
  Person,
  Reservation,
  ReservationView,
  Room,
  RoomSchedule,
} from './api-models';

/**
 * All HTTP access to the booking backend. Read methods return resources that
 * refetch when their signal arguments change, so they must be called from an
 * injection context (e.g. a component field initializer). Writes return observables.
 */
@Injectable({ providedIn: 'root' })
export class BookingApi {
  private readonly http = inject(HttpClient);

  rooms() {
    return httpResource<Room[]>(() => '/api/rooms');
  }

  room(roomId: () => number) {
    return httpResource<Room>(() => `/api/rooms/${roomId()}`);
  }

  people() {
    return httpResource<Person[]>(() => '/api/people');
  }

  /** Waits (idle) until a date is known. */
  schedule(roomId: () => number, date: () => string | undefined) {
    return httpResource<RoomSchedule>(() => {
      const day = date();
      return day ? { url: `/api/rooms/${roomId()}/schedule`, params: { date: day } } : undefined;
    });
  }

  reservations(view: () => ReservationView) {
    return httpResource<Reservation[]>(() => ({ url: '/api/reservations', params: { view: view() } }));
  }

  reservation(id: () => number) {
    return httpResource<Reservation>(() => `/api/reservations/${id()}`);
  }

  /** A preview only: the answer can be stale by the time a booking is submitted. */
  checkAvailability(roomId: number, start: string, end: string): Observable<Availability> {
    return this.http.get<Availability>(`/api/rooms/${roomId}/availability`, {
      params: { start, end },
    });
  }

  /** Not retried automatically: a lost response may still have created the booking. */
  createReservation(request: CreateReservationRequest): Observable<Reservation> {
    return this.http.post<Reservation>('/api/reservations', request);
  }

  cancelReservation(id: number): Observable<Reservation> {
    return this.http.post<Reservation>(`/api/reservations/${id}/cancel`, null);
  }

  resetDemoData(): Observable<DemoResetSummary> {
    return this.http.post<DemoResetSummary>('/api/demo/reset', null);
  }
}
