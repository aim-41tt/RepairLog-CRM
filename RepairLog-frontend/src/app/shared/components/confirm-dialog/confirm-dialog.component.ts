import { Component, inject } from '@angular/core';
import { ConfirmService } from '../../../core/services/confirm.service';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  template: `
    @if (confirmService.state(); as s) {
      <div class="overlay">
        <div class="dialog dialog--{{ s.variant }}">
          <h3 class="dialog__title">{{ s.title }}</h3>
          <p class="dialog__message">{{ s.message }}</p>
          <div class="dialog__actions">
            <button class="btn btn--ghost" (click)="s.resolve(false)">{{ s.cancelLabel }}</button>
            <button class="btn btn--{{ s.variant === 'danger' ? 'danger' : 'primary' }}" (click)="s.resolve(true)">
              {{ s.confirmLabel }}
            </button>
          </div>
        </div>
      </div>
    }
  `,
  styles: [`
    .overlay {
      position: fixed;
      inset: 0;
      z-index: 10000;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(0, 0, 0, .5);
      animation: fadeIn .15s ease;
    }

    .dialog {
      background: var(--surface, #fff);
      border-radius: 12px;
      padding: 24px;
      max-width: 420px;
      width: 90%;
      box-shadow: 0 8px 32px rgba(0, 0, 0, .3);
      animation: scaleIn .2s ease;
    }

    .dialog__title {
      margin: 0 0 8px;
      font-size: 18px;
      color: var(--text);
    }

    .dialog__message {
      margin: 0 0 24px;
      font-size: 14px;
      color: var(--text-muted);
      line-height: 1.5;
    }

    .dialog__actions {
      display: flex;
      gap: 8px;
      justify-content: flex-end;
    }

    .btn--danger {
      background: var(--danger, #d32f2f);
      color: #fff;
      border: none;
      padding: 8px 16px;
      border-radius: 6px;
      cursor: pointer;
      font-size: 13px;
      font-weight: 500;

      &:hover { opacity: .9; }
    }

    @keyframes fadeIn {
      from { opacity: 0; }
      to   { opacity: 1; }
    }

    @keyframes scaleIn {
      from { transform: scale(.95); opacity: 0; }
      to   { transform: scale(1);   opacity: 1; }
    }
  `]
})
export class ConfirmDialogComponent {
  confirmService = inject(ConfirmService);
}
