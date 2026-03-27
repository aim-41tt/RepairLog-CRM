import { Component, inject } from '@angular/core';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  template: `
    <div class="toast-container">
      @for (t of toastService.toasts(); track t.id) {
        <div class="toast toast--{{ t.type }}" (click)="toastService.dismiss(t.id)">
          <span class="toast__message">{{ t.message }}</span>
          <button class="toast__close">&times;</button>
        </div>
      }
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 16px;
      right: 16px;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 8px;
      max-width: 420px;
    }

    .toast {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      padding: 12px 16px;
      border-radius: 8px;
      color: #fff;
      font-size: 13px;
      box-shadow: 0 4px 12px rgba(0,0,0,.25);
      cursor: pointer;
      animation: slideIn .25s ease;

      &--success { background: #2e7d32; }
      &--error   { background: #c62828; }
      &--warning { background: #e65100; }
      &--info    { background: #1565c0; }
    }

    .toast__message { flex: 1; }
    .toast__close {
      background: none;
      border: none;
      color: inherit;
      font-size: 18px;
      cursor: pointer;
      line-height: 1;
      opacity: .7;
      &:hover { opacity: 1; }
    }

    @keyframes slideIn {
      from { transform: translateX(100%); opacity: 0; }
      to   { transform: translateX(0);    opacity: 1; }
    }
  `]
})
export class ToastComponent {
  toastService = inject(ToastService);
}
