import { Component, ElementRef, input, output, viewChild } from '@angular/core';

let nextId = 0;

/**
 * A modal confirmation built on the native <dialog> element, which provides
 * focus trapping and Escape-to-close. Projected content explains the action.
 */
@Component({
  selector: 'app-confirm-dialog',
  template: `
    <dialog #dialog [attr.aria-labelledby]="headingId" (close)="onClose()">
      <h2 [id]="headingId">{{ heading() }}</h2>
      <ng-content />
      <div class="actions">
        <button type="button" autofocus (click)="dialog.close('cancel')">{{ cancelLabel() }}</button>
        <button type="button" class="danger" (click)="dialog.close('confirm')">
          {{ confirmLabel() }}
        </button>
      </div>
    </dialog>
  `,
  styles: `
    dialog {
      max-width: 28rem;
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 1.25rem 1.5rem;
    }
    dialog::backdrop {
      background: rgb(15 23 42 / 0.45);
    }
    h2 {
      margin-top: 0;
      font-size: 1.15rem;
    }
    .actions {
      display: flex;
      justify-content: flex-end;
      gap: 0.5rem;
      margin-top: 1.25rem;
    }
  `,
})
export class ConfirmDialog {
  readonly heading = input.required<string>();
  readonly confirmLabel = input('Confirm');
  readonly cancelLabel = input('Cancel');
  readonly confirmed = output<void>();

  protected readonly headingId = `confirm-dialog-${nextId++}`;
  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  open(): void {
    const dialog = this.dialog().nativeElement;
    dialog.returnValue = '';
    dialog.showModal();
  }

  protected onClose(): void {
    if (this.dialog().nativeElement.returnValue === 'confirm') {
      this.confirmed.emit();
    }
  }
}
