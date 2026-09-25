import { HttpErrorResponse } from '@angular/common/http';

import { Reservation } from './api-models';

export type ApiErrorKind = 'validation' | 'conflict' | 'not-found' | 'unreachable' | 'server';

/** A backend failure translated into something the UI can show. */
export interface ApiError {
  kind: ApiErrorKind;
  status: number;
  /** Stable problem code from the backend, e.g. "reservation_conflict". */
  code?: string;
  message: string;
  /** Validation messages by request field name. */
  fieldErrors: Record<string, string[]>;
  conflicts: Reservation[];
}

interface ProblemBody {
  detail?: string;
  code?: string;
  errors?: { field: string; message: string }[];
  conflicts?: Reservation[];
}

export function toApiError(error: unknown): ApiError {
  const response = error instanceof HttpErrorResponse ? error : undefined;
  const status = response?.status ?? 0;
  const body: ProblemBody =
    response && typeof response.error === 'object' && response.error !== null ? response.error : {};

  const fieldErrors: Record<string, string[]> = {};
  for (const { field, message } of body.errors ?? []) {
    (fieldErrors[field] ??= []).push(message);
  }

  return {
    kind: kindFor(status),
    status,
    code: body.code,
    message: messageFor(status, body.detail),
    fieldErrors,
    conflicts: body.conflicts ?? [],
  };
}

/** No response at all, or a gateway (such as the dev-server proxy) could not reach the backend. */
const UNREACHABLE_STATUSES = [0, 502, 503, 504];

function kindFor(status: number): ApiErrorKind {
  if (status === 400) return 'validation';
  if (status === 404) return 'not-found';
  if (status === 409) return 'conflict';
  if (UNREACHABLE_STATUSES.includes(status)) return 'unreachable';
  return 'server';
}

function messageFor(status: number, detail: string | undefined): string {
  if (UNREACHABLE_STATUSES.includes(status)) {
    return 'The booking service could not be reached. Check that the backend is running.';
  }
  if (status >= 500 || !detail) {
    return `The booking service reported an error (HTTP ${status}).`;
  }
  return detail;
}
