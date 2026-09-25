import { Venue } from '../../core/api-models';
import { checkBookingTimes } from './booking-rules';

const venue: Venue = {
  name: 'Test venue',
  zone: 'Europe/London',
  opensAt: '08:00',
  closesAt: '20:00',
  slotMinutes: 15,
  bookingHorizonDays: 90,
  now: '2026-10-20T10:00:00+01:00',
  today: '2026-10-20',
  lastBookableDate: '2027-01-18',
};
const now = { date: '2026-10-20', time: '10:05' };

describe('checkBookingTimes', () => {
  it('accepts a future range', () => {
    expect(checkBookingTimes({ date: '2026-10-21', start: '09:00', end: '10:00' }, venue, now)).toEqual({});
  });

  it('rejects an end at or before the start', () => {
    expect(checkBookingTimes({ date: '2026-10-21', start: '10:00', end: '10:00' }, venue, now).end).toBeDefined();
  });

  it('rejects a start that has already passed today, using venue time', () => {
    expect(checkBookingTimes({ date: '2026-10-20', start: '10:00', end: '11:00' }, venue, now).start).toBeDefined();
    expect(checkBookingTimes({ date: '2026-10-20', start: '10:15', end: '11:00' }, venue, now)).toEqual({});
  });

  it('rejects dates before today or beyond the booking horizon', () => {
    expect(checkBookingTimes({ date: '2026-10-19', start: '', end: '' }, venue, now).date).toBeDefined();
    expect(checkBookingTimes({ date: '2027-01-19', start: '', end: '' }, venue, now).date).toBeDefined();
    expect(checkBookingTimes({ date: '2027-01-18', start: '', end: '' }, venue, now)).toEqual({});
  });
});
