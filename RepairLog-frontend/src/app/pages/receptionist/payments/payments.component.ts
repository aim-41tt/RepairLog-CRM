import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { OrderService } from '../../../core/services/order.service';
import { ReceiptService } from '../../../core/services/receipt.service';
import { PaymentService } from '../../../core/services/payment.service';
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
  private fb = inject(FormBuilder);

  searchQuery = signal('');
  order = signal<Order | null>(null);
  receipt = signal<Receipt | null>(null);
  showPayForm = signal(false);

  payForm = this.fb.nonNullable.group({
    paidAmount: [0, [Validators.required, Validators.min(0.01)]],
    paymentMethod: ['CASH', Validators.required]
  });

  search(): void {
    const q = this.searchQuery();
    if (!q.trim()) return;
    this.orderService.searchByNumber(q).subscribe({
      next: orders => {
        if (orders.length) {
          this.order.set(orders[0]);
          this.receiptService.getByOrder(orders[0].id, 'receptionist').subscribe({
            next: r => this.receipt.set(r),
            error: () => this.receipt.set(null)
          });
        }
      }
    });
  }

  processPayment(): void {
    if (this.payForm.invalid || !this.receipt()) return;
    const v = this.payForm.getRawValue();
    this.paymentService.createPayment({ receiptId: this.receipt()!.id, paidAmount: v.paidAmount, paymentMethod: v.paymentMethod }).subscribe({
      next: () => { this.showPayForm.set(false); this.search(); }
    });
  }

  paymentStatusBadge(s: string): string {
    if (s === 'FULLY_PAID') return 'badge--success';
    if (s === 'PARTIALLY_PAID') return 'badge--warning';
    if (s === 'REFUNDED') return 'badge--info';
    return 'badge--muted';
  }

  paymentStatusLabel(s: string): string {
    const m: Record<string, string> = { UNPAID: 'Не оплачено', PARTIALLY_PAID: 'Частично', FULLY_PAID: 'Оплачено', REFUNDED: 'Возврат' };
    return m[s] ?? s;
  }
}
