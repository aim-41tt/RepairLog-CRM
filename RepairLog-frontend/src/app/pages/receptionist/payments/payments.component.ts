import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { OrderService } from '../../../core/services/order.service';
import { ReceiptService } from '../../../core/services/receipt.service';
import { PaymentService } from '../../../core/services/payment.service';
import { ToastService } from '../../../core/services/toast.service';
import { Order } from '../../../core/models/order.models';
import { Receipt } from '../../../core/models/receipt.models';

@Component({
  selector: 'app-payments',
  standalone: true,
  imports: [PageLayoutComponent, ReactiveFormsModule, RouterLink],
  templateUrl: './payments.component.html',
  styleUrl: './payments.component.scss'
})
export class PaymentsComponent {
  private orderService = inject(OrderService);
  private receiptService = inject(ReceiptService);
  private paymentService = inject(PaymentService);
  private toast = inject(ToastService);
  private fb = inject(FormBuilder);

  searchQuery = signal('');
  order = signal<Order | null>(null);
  receipt = signal<Receipt | null>(null);
  showPayForm = signal(false);
  /** B-04: prevents double-submit while request is in flight */
  submitting = signal(false);

  payForm = this.fb.nonNullable.group({
    paidAmount: [0, [Validators.required, Validators.min(0.01)]],
    paymentMethod: ['CASH', Validators.required],
    transactionId: ['']
  });

  /** Returns true when paymentMethod requires a transaction reference (CARD/TRANSFER). */
  needsTransactionId = (): boolean => {
    const m = this.payForm.get('paymentMethod')?.value;
    return m === 'CARD' || m === 'TRANSFER';
  };

  search(): void {
    const q = this.searchQuery();
    if (!q.trim()) return;
    this.order.set(null);
    this.receipt.set(null);
    this.orderService.searchByNumber(q).subscribe({
      next: orders => {
        if (orders.length) {
          this.order.set(orders[0]);
          this.receiptService.getByOrder(orders[0].id, 'receptionist').subscribe({
            next: r => this.receipt.set(r),
            error: () => this.receipt.set(null)
          });
        }
      },
      error: () => { this.order.set(null); this.receipt.set(null); }
    });
  }

  processPayment(): void {
    // B-04: guard against double-click / duplicate submissions
    if (this.payForm.invalid || !this.receipt() || this.submitting()) return;
    this.submitting.set(true);
    const v = this.payForm.getRawValue();
    this.paymentService.createPayment({
      receiptId: this.receipt()!.id,
      paidAmount: v.paidAmount,
      paymentMethod: v.paymentMethod,
      transactionId: v.transactionId || undefined
    }).subscribe({
      next: () => {
        this.toast.success('Оплата принята');
        this.showPayForm.set(false);
        this.submitting.set(false);
        this.search();
      },
      error: (err) => {
        this.submitting.set(false);
        this.toast.error(err?.error?.message ?? 'Ошибка приёма оплаты');
      }
    });
  }

  // B-06: badge CSS class with null-safe fallback
  paymentStatusBadge(s: string | null | undefined): string {
    if (!s || s === 'UNPAID') return 'badge--muted';
    if (s === 'FULLY_PAID') return 'badge--success';
    if (s === 'PARTIALLY_PAID') return 'badge--warning';
    if (s === 'REFUNDED') return 'badge--info';
    return 'badge--muted';
  }

  // B-06: label with null-safe fallback
  paymentStatusLabel(s: string | null | undefined): string {
    if (!s) return 'Не оплачено';
    const m: Record<string, string> = {
      UNPAID: 'Не оплачено',
      PARTIALLY_PAID: 'Частично оплачено',
      FULLY_PAID: 'Оплачено',
      REFUNDED: 'Возврат'
    };
    return m[s] ?? s;
  }
}
