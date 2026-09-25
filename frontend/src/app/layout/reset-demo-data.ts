import { Component, inject, signal, viewChild } from '@angular/core';
import { Router } from '@angular/router';

import { toApiError } from '../core/api-error';
import { BookingApi } from '../core/booking-api';
import { ConfirmDialog } from '../shared/confirm-dialog';

/** Footer control that restores the fictional demo dataset after confirmation. */
@Component({
  selector: 'app-reset-demo-data',
  imports: [ConfirmDialog],
  template: `
    <button type="button" class="link-button" [disabled]="resetting()" (click)="dialog().open()">
      {{ resetting() ? 'Resetting…' : 'Reset demo data' }}
    </button>
    @if (error()) {
      <span class="inline-error" role="alert">{{ error() }}</span>
    }
    <app-confirm-dialog
      heading="Reset demo data?"
      confirmLabel="Reset demo data"
      cancelLabel="Keep current data"
      (confirmed)="reset()"
    >
      <p>
        This deletes every reservation, including ones you made, and restores the fictional rooms,
        people, and sample reservations. It cannot be undone.
      </p>
    </app-confirm-dialog>
  `,
  styles: `
    :host {
      display: inline-flex;
      align-items: center;
      gap: 0.75rem;
    }
  `,
})
export class ResetDemoData {
  private readonly api = inject(BookingApi);
  private readonly router = inject(Router);

  protected readonly dialog = viewChild.required(ConfirmDialog);
  protected readonly resetting = signal(false);
  protected readonly error = signal<string | null>(null);

  protected reset(): void {
    this.resetting.set(true);
    this.error.set(null);
    this.api.resetDemoData().subscribe({
      next: () => {
        // Leave and re-enter the route so every page reloads its data.
        this.router
          .navigateByUrl('/', { skipLocationChange: true })
          .then(() => this.router.navigate(['/rooms'], { queryParams: { reset: 'done' } }))
          .finally(() => this.resetting.set(false));
      },
      error: (err) => {
        this.error.set(`Reset failed. ${toApiError(err).message}`);
        this.resetting.set(false);
      },
    });
  }
}
