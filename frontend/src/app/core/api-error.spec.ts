import { HttpErrorResponse } from '@angular/common/http';

import { toApiError } from './api-error';

describe('toApiError', () => {
  it('groups validation messages by field', () => {
    const error = toApiError(
      new HttpErrorResponse({
        status: 400,
        error: {
          code: 'validation_failed',
          detail: 'Some fields are missing or invalid.',
          errors: [
            { field: 'end', message: 'End time must be after the start time.' },
            { field: 'end', message: 'End time must be within opening hours 08:00–20:00.' },
            { field: 'title', message: 'Enter a title.' },
          ],
        },
      }),
    );

    expect(error.kind).toBe('validation');
    expect(error.fieldErrors['end']).toHaveLength(2);
    expect(error.fieldErrors['title']).toEqual(['Enter a title.']);
  });

  it('keeps conflicting reservations from a 409', () => {
    const error = toApiError(
      new HttpErrorResponse({
        status: 409,
        error: { code: 'reservation_conflict', detail: 'Booked.', conflicts: [{ id: 7 }] },
      }),
    );

    expect(error.kind).toBe('conflict');
    expect(error.code).toBe('reservation_conflict');
    expect(error.conflicts.map((c) => c.id)).toEqual([7]);
  });

  it('treats a missing response or a failed proxy as unreachable', () => {
    const error = toApiError(new HttpErrorResponse({ status: 0, error: new ProgressEvent('error') }));
    expect(error.kind).toBe('unreachable');
    expect(error.message).toContain('could not be reached');

    expect(toApiError(new HttpErrorResponse({ status: 502 })).kind).toBe('unreachable');
  });

  it('does not show raw server detail for 5xx errors', () => {
    const error = toApiError(new HttpErrorResponse({ status: 500, error: { detail: 'NullPointer…' } }));

    expect(error.kind).toBe('server');
    expect(error.message).toBe('The booking service reported an error (HTTP 500).');
  });
});
