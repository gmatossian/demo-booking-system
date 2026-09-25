import { httpResource } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Venue } from './api-models';

/** Venue rules (timezone, hours, today's date) loaded once from the backend. */
@Injectable({ providedIn: 'root' })
export class VenueService {
  readonly venue = httpResource<Venue>(() => '/api/venue');
}
