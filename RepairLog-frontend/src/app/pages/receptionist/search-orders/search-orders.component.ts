import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { OrderService } from '../../../core/services/order.service';
import { ReferenceService } from '../../../core/services/reference.service';
import { Order, StatusHistoryEntry } from '../../../core/models/order.models';
import { RepairStatus } from '../../../core/models/reference.models';
import { Diagnostic } from '../../../core/models/diagnostic.models';
import { Receipt } from '../../../core/models/receipt.models';
import { ConfirmService } from '../../../core/services/confirm.service';
import { DocumentService } from '../../../core/services/document.service';

@Component({
  selector: 'app-search-orders',
  standalone: true,
  imports: [PageLayoutComponent, RouterLink, DatePipe],
  templateUrl: './search-orders.component.html',
  styleUrl: './search-orders.component.scss'
})
export class SearchOrdersComponent implements OnInit {
  private orderService = inject(OrderService);
  private refService = inject(ReferenceService);
  private confirmService = inject(ConfirmService);
  documentService = inject(DocumentService);

  orders = signal<Order[]>([]);
  statuses = signal<RepairStatus[]>([]);
  searchQuery = signal('');
  selectedOrderId = signal<number | null>(null);
  selectedStatusId = signal(0);

  // Details expansion
  expandedOrderId = signal<number | null>(null);
  orderDiagnostic = signal<Diagnostic | null>(null);
  orderReceipt = signal<Receipt | null>(null);
  statusHistory = signal<StatusHistoryEntry[]>([]);
  detailsLoading = signal(false);

  ngOnInit(): void {
    this.refService.getRepairStatuses().subscribe({ next: s => this.statuses.set(s) });
  }

  search(): void {
    const q = this.searchQuery();
    if (!q.trim()) return;
    this.orderService.searchMulti(q).subscribe({ next: r => this.orders.set(r) });
  }

  changeStatus(orderId: number): void {
    const statusId = this.selectedStatusId();
    if (!statusId) return;
    const status = this.statuses().find(s => s.id === statusId);
    const name = status?.name ?? `#${statusId}`;
    const dangerStatuses = ['Отменен', 'Выдан'];
    this.confirmService.confirm({
      title: 'Смена статуса',
      message: `Изменить статус заявки на «${name}»?`,
      confirmLabel: 'Изменить',
      variant: dangerStatuses.includes(name) ? 'danger' : 'warning'
    }).subscribe(ok => {
      if (!ok) return;
      this.orderService.updateStatusReceptionist(orderId, { statusId }).subscribe({
        next: () => this.search()
      });
    });
  }

  toggleDetails(orderId: number): void {
    if (this.expandedOrderId() === orderId) {
      this.expandedOrderId.set(null);
      return;
    }
    this.expandedOrderId.set(orderId);
    this.orderDiagnostic.set(null);
    this.orderReceipt.set(null);
    this.statusHistory.set([]);
    this.detailsLoading.set(true);

    this.orderService.getStatusHistory(orderId, 'receptionist').subscribe({
      next: h => this.statusHistory.set(h),
      error: () => {}
    });
    this.orderService.getOrderDiagnosticsReceptionist(orderId).subscribe({
      next: d => this.orderDiagnostic.set(d),
      error: () => {}
    });
    this.orderService.getReceiptReceptionist(orderId).subscribe({
      next: r => { this.orderReceipt.set(r); this.detailsLoading.set(false); },
      error: () => this.detailsLoading.set(false)
    });
  }

  paymentBadge(s: string): string {
    if (s === 'FULLY_PAID') return 'badge--success';
    if (s === 'PARTIALLY_PAID') return 'badge--warning';
    return 'badge--muted';
  }

  paymentLabel(s: string): string {
    const m: Record<string, string> = { UNPAID: 'Не оплачено', PARTIALLY_PAID: 'Частично', FULLY_PAID: 'Оплачено', REFUNDED: 'Возврат' };
    return m[s] ?? s;
  }
}
