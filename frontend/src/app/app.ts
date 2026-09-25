import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { VenueService } from './core/venue.service';
import { ResetDemoData } from './layout/reset-demo-data';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ResetDemoData],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly venue = inject(VenueService).venue;
}
