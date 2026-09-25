/** Shapes returned by the booking API. See docs/api.md. */

export interface Venue {
  name: string;
  /** IANA timezone used for every booking rule and displayed time. */
  zone: string;
  opensAt: string; // "08:00"
  closesAt: string; // "20:00"
  slotMinutes: number;
  bookingHorizonDays: number;
  now: string;
  today: string; // "2026-10-20", venue-local
  lastBookableDate: string;
}

export interface Room {
  id: number;
  name: string;
  location: string;
  capacity: number;
  equipment: string[];
}

export interface Person {
  id: number;
  displayName: string;
}

export type ReservationStatus = 'ACTIVE' | 'CANCELLED';
export type ReservationPhase = 'UPCOMING' | 'IN_PROGRESS' | 'ENDED';

export interface Reservation {
  id: number;
  roomId: number;
  roomName: string;
  personId: number;
  personName: string;
  title: string;
  /** ISO-8601 with the venue's offset, e.g. "2026-10-21T09:00:00+01:00". */
  start: string;
  end: string;
  status: ReservationStatus;
  phase: ReservationPhase;
  cancellable: boolean;
  createdAt: string;
  cancelledAt: string | null;
}

export interface RoomSchedule {
  date: string;
  reservations: Reservation[];
}

export interface Availability {
  available: boolean;
  conflicts: Reservation[];
}

/** Start and end are venue-local wall-clock times, e.g. "2026-10-21T09:00". */
export interface CreateReservationRequest {
  roomId: number;
  personId: number;
  title: string;
  start: string;
  end: string;
}

export interface DemoResetSummary {
  anchorDate: string;
  rooms: number;
  people: number;
  reservations: number;
}

export type ReservationView = 'upcoming' | 'past' | 'cancelled' | 'all';
