import { addDays, formatDate, formatTime, slotTimes, venueNow } from './venue-time';

describe('venue-time', () => {
  it('formats instants in the venue timezone, not the runtime timezone', () => {
    // 08:00 UTC is 09:00 in London (BST) but 04:00 in New York.
    expect(formatTime('2026-10-24T08:00:00Z', 'Europe/London')).toBe('09:00');
    expect(formatTime('2026-10-24T08:00:00Z', 'America/New_York')).toBe('04:00');
  });

  it('uses the offset in force on each side of the clock change', () => {
    expect(formatTime('2026-10-24T09:00:00+01:00', 'Europe/London')).toBe('09:00');
    expect(formatTime('2026-10-25T09:00:00Z', 'Europe/London')).toBe('09:00');
  });

  it('formats the venue-local date even when UTC is on a different day', () => {
    // 23:30 UTC on 24 Oct is 00:30 on 25 Oct in London during BST.
    expect(formatDate('2026-10-24T23:30:00Z', 'Europe/London')).toMatch(/^Sun,? 25 Oct 2026$/);
    expect(venueNow('Europe/London', new Date('2026-10-24T23:30:00Z'))).toEqual({
      date: '2026-10-25',
      time: '00:30',
    });
  });

  it('lists slot boundaries from opening to closing inclusive', () => {
    const slots = slotTimes('08:00', '20:00', 15);
    expect(slots[0]).toBe('08:00');
    expect(slots[1]).toBe('08:15');
    expect(slots.at(-1)).toBe('20:00');
    expect(slots).toHaveLength(49);
  });

  it('adds calendar days across month ends', () => {
    expect(addDays('2026-10-31', 1)).toBe('2026-11-01');
    expect(addDays('2026-10-25', -1)).toBe('2026-10-24');
  });
});
