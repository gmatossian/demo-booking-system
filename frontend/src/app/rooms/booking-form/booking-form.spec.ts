import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Subject } from 'rxjs';

import { Availability, Person, Reservation, Room, Venue } from '../../core/api-models';
import { BookingApi } from '../../core/booking-api';
import { addDays, venueNow } from '../../core/venue-time';
import { BookingForm } from './booking-form';

const ZONE = 'Europe/London';
const TODAY = venueNow(ZONE).date;
const TOMORROW = addDays(TODAY, 1);

const VENUE: Venue = {
  name: 'Test venue',
  zone: ZONE,
  opensAt: '08:00',
  closesAt: '20:00',
  slotMinutes: 15,
  bookingHorizonDays: 90,
  now: `${TODAY}T09:00:00Z`,
  today: TODAY,
  lastBookableDate: addDays(TODAY, 90),
};
const ROOM: Room = { id: 1, name: 'Aurora', location: 'Floor 1', capacity: 4, equipment: [] };
const PEOPLE: Person[] = [{ id: 1, displayName: 'Avery Quinn' }];

function reservation(id: number, title: string): Reservation {
  return {
    id,
    roomId: 1,
    roomName: 'Aurora',
    personId: 1,
    personName: 'Avery Quinn',
    title,
    start: `${TOMORROW}T10:00:00+01:00`,
    end: `${TOMORROW}T11:00:00+01:00`,
    status: 'ACTIVE',
    phase: 'UPCOMING',
    cancellable: true,
    createdAt: `${TODAY}T09:00:00Z`,
    cancelledAt: null,
  };
}

/**
 * Each API call returns a Subject the test resolves later, simulating slow
 * responses that arrive after the user has moved on.
 */
class DelayedBookingApi {
  readonly checks: Subject<Availability>[] = [];
  readonly creates: Subject<Reservation>[] = [];

  checkAvailability() {
    const response = new Subject<Availability>();
    this.checks.push(response);
    return response;
  }

  createReservation() {
    const response = new Subject<Reservation>();
    this.creates.push(response);
    return response;
  }
}

describe('BookingForm availability preview', () => {
  let fixture: ComponentFixture<BookingForm>;
  let api: DelayedBookingApi;

  beforeEach(async () => {
    api = new DelayedBookingApi();
    TestBed.configureTestingModule({
      imports: [BookingForm],
      providers: [provideRouter([]), { provide: BookingApi, useValue: api }],
    });
    fixture = TestBed.createComponent(BookingForm);
    fixture.componentRef.setInput('room', ROOM);
    fixture.componentRef.setInput('people', PEOPLE);
    fixture.componentRef.setInput('venue', VENUE);
    fixture.componentRef.setInput('date', TOMORROW);
    await fixture.whenStable();
    await choose('#booking-start', '10:00'); // end is suggested as 11:00
  });

  it('ignores a late success after the inputs change', async () => {
    await click('Check availability');
    expect(text()).toContain('Checking');

    await type('#booking-title', 'Changed my mind');
    await respond(() => api.checks[0].next({ available: true, conflicts: [] }));

    expect(api.checks[0].observed).toBe(false);
    expect(text()).not.toContain('Checking');
    expect(text()).not.toContain('The room is free');
  });

  it('ignores a late error after the inputs change', async () => {
    await click('Check availability');

    await choose('#booking-end', '11:30');
    await respond(() =>
      api.checks[0].error(validationError('start', 'Start time has already passed.')),
    );

    expect(api.checks[0].observed).toBe(false);
    expect(text()).not.toContain('could not be checked');
    expect(text()).not.toContain('Start time has already passed.');
  });

  it('shows only the newest check when an older one answers last', async () => {
    await click('Check availability');
    await click('Check availability');
    expect(api.checks).toHaveLength(2);
    expect(api.checks[0].observed).toBe(false);

    await respond(() => api.checks[1].next({ available: true, conflicts: [] }));
    await respond(() =>
      api.checks[0].next({ available: false, conflicts: [reservation(9, 'Old')] }),
    );

    expect(text()).toContain('The room is free');
    expect(text()).not.toContain('already booked');
  });

  it('drops a pending check when booking starts', async () => {
    await type('#booking-title', 'Planning');
    await chooseIndex('#booking-person', 1);
    await click('Check availability');

    await click('Book room');
    expect(api.creates).toHaveLength(1);
    expect(api.checks[0].observed).toBe(false);

    await respond(() =>
      api.checks[0].next({ available: false, conflicts: [reservation(9, 'Old')] }),
    );
    await respond(() => {
      api.creates[0].next(reservation(10, 'Planning'));
      api.creates[0].complete();
    });

    expect(text()).toContain('Booked “Planning”');
    expect(text()).not.toContain('already booked');
    expect(text()).not.toContain('Checking');
  });

  function text(): string {
    return (fixture.nativeElement as HTMLElement).textContent ?? '';
  }

  function query<T extends HTMLElement>(selector: string): T {
    return (fixture.nativeElement as HTMLElement).querySelector<T>(selector)!;
  }

  async function type(selector: string, value: string): Promise<void> {
    const input = query<HTMLInputElement>(selector);
    input.value = value;
    input.dispatchEvent(new Event('input'));
    await fixture.whenStable();
  }

  async function choose(selector: string, value: string): Promise<void> {
    const select = query<HTMLSelectElement>(selector);
    select.value = value;
    select.dispatchEvent(new Event('change'));
    await fixture.whenStable();
  }

  async function chooseIndex(selector: string, index: number): Promise<void> {
    const select = query<HTMLSelectElement>(selector);
    select.selectedIndex = index;
    select.dispatchEvent(new Event('change'));
    await fixture.whenStable();
  }

  async function click(label: string): Promise<void> {
    const buttons = Array.from((fixture.nativeElement as HTMLElement).querySelectorAll('button'));
    buttons.find((button) => button.textContent?.trim() === label)!.click();
    await fixture.whenStable();
  }

  async function respond(deliver: () => void): Promise<void> {
    deliver();
    await fixture.whenStable();
  }

  function validationError(field: string, message: string): HttpErrorResponse {
    return new HttpErrorResponse({
      status: 400,
      error: { code: 'validation_failed', detail: 'Invalid.', errors: [{ field, message }] },
    });
  }
});
