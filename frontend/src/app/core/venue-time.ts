/**
 * Date/time helpers that always use the venue timezone passed in, never the
 * browser's own timezone. Dates are "YYYY-MM-DD" and times "HH:mm" strings.
 */

const LOCALE = 'en-GB';

/** "09:00" for an ISO instant, shown in the venue timezone. */
export function formatTime(iso: string, zone: string): string {
  return new Intl.DateTimeFormat(LOCALE, {
    timeZone: zone,
    hour: '2-digit',
    minute: '2-digit',
    hourCycle: 'h23',
  }).format(new Date(iso));
}

/** "Wed, 21 Oct 2026" (browser-dependent punctuation) for an ISO instant, shown in the venue timezone. */
export function formatDate(iso: string, zone: string): string {
  return new Intl.DateTimeFormat(LOCALE, {
    timeZone: zone,
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  }).format(new Date(iso));
}

/** "Wed, 21 Oct 2026, 14:05" for an ISO instant, shown in the venue timezone. */
export function formatDateTime(iso: string, zone: string): string {
  return `${formatDate(iso, zone)}, ${formatTime(iso, zone)}`;
}

/** "Wednesday 21 October 2026" for a calendar date (no timezone involved). */
export function formatCalendarDate(date: string): string {
  return new Intl.DateTimeFormat(LOCALE, {
    timeZone: 'UTC',
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(calendarDateAsUtc(date));
}

export function addDays(date: string, days: number): string {
  const utc = calendarDateAsUtc(date);
  utc.setUTCDate(utc.getUTCDate() + days);
  return utc.toISOString().slice(0, 10);
}

/** Every slot boundary from opening to closing inclusive, e.g. ["08:00", "08:15", …, "20:00"]. */
export function slotTimes(opensAt: string, closesAt: string, slotMinutes: number): string[] {
  const times: string[] = [];
  for (let minutes = toMinutes(opensAt); minutes <= toMinutes(closesAt); minutes += slotMinutes) {
    times.push(fromMinutes(minutes));
  }
  return times;
}

export function toMinutes(time: string): number {
  const [hours, minutes] = time.split(':').map(Number);
  return hours * 60 + minutes;
}

export function fromMinutes(total: number): string {
  const hours = Math.floor(total / 60);
  const minutes = total % 60;
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`;
}

/** The current venue-local date and time, e.g. { date: "2026-10-20", time: "10:05" }. */
export function venueNow(zone: string, now: Date = new Date()): { date: string; time: string } {
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone: zone,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hourCycle: 'h23',
  }).formatToParts(now);
  const part = (type: Intl.DateTimeFormatPartTypes) => parts.find((p) => p.type === type)?.value ?? '';
  return {
    date: `${part('year')}-${part('month')}-${part('day')}`,
    time: `${part('hour')}:${part('minute')}`,
  };
}

function calendarDateAsUtc(date: string): Date {
  const [year, month, day] = date.split('-').map(Number);
  return new Date(Date.UTC(year, month - 1, day));
}
