import { Injectable, signal } from '@angular/core';
import { Observable, Subject } from 'rxjs';

export type ConfirmVariant = 'danger' | 'warning' | 'info';

export interface ConfirmOptions {
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  variant?: ConfirmVariant;
}

export interface ConfirmState extends ConfirmOptions {
  resolve: (value: boolean) => void;
}

@Injectable({ providedIn: 'root' })
export class ConfirmService {
  readonly state = signal<ConfirmState | null>(null);

  confirm(options: ConfirmOptions): Observable<boolean> {
    const subject = new Subject<boolean>();
    this.state.set({
      ...options,
      confirmLabel: options.confirmLabel ?? 'Подтвердить',
      cancelLabel: options.cancelLabel ?? 'Отмена',
      variant: options.variant ?? 'warning',
      resolve: (value: boolean) => {
        subject.next(value);
        subject.complete();
        this.state.set(null);
      }
    });
    return subject.asObservable();
  }
}
