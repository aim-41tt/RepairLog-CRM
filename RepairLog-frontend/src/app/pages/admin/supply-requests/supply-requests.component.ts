import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { SupplyRequestService } from '../../../core/services/supply-request.service';
import { SupplierService } from '../../../core/services/supplier.service';
import { ToastService } from '../../../core/services/toast.service';
import { SupplyRequest, SupplyRequestStatus, SupplierPaymentResponse, SupplierInvoiceResponse } from '../../../core/models/supply-request.models';
import { Supplier } from '../../../core/models/supplier.models';

@Component({
  selector: 'app-admin-supply-requests',
  standalone: true,
  imports: [PageLayoutComponent, RouterLink, ReactiveFormsModule, DatePipe],
  templateUrl: './supply-requests.component.html',
  styleUrl: './supply-requests.component.scss'
})
export class AdminSupplyRequestsComponent implements OnInit {
  private supplyService = inject(SupplyRequestService);
  private supplierService = inject(SupplierService);
  private toast = inject(ToastService);
  private fb = inject(FormBuilder);

  requests = signal<SupplyRequest[]>([]);
  suppliers = signal<Supplier[]>([]);
  loading = signal(false);
  activeFilter = signal<SupplyRequestStatus | 'ALL'>('ALL');
  expandedId = signal<number | null>(null);

  // Payments & invoices for expanded request
  payments = signal<SupplierPaymentResponse[]>([]);
  invoices = signal<SupplierInvoiceResponse[]>([]);
  showPaymentForm = signal(false);
  showInvoiceForm = signal(false);

  // Add item form
  addItemForm = this.fb.nonNullable.group({
    itemName: ['', Validators.required],
    quantity: [1, [Validators.required, Validators.min(1)]],
    unitPrice: [0]
  });

  // Payment form
  paymentForm = this.fb.nonNullable.group({
    amount: [0, [Validators.required, Validators.min(0.01)]],
    method: ['CASH', Validators.required],
    note: ['']
  });

  // Invoice form
  invoiceForm = this.fb.nonNullable.group({
    invoiceNumber: ['', Validators.required],
    amount: [0, [Validators.required, Validators.min(0.01)]],
    issuedAt: ['', Validators.required]
  });

  statuses: (SupplyRequestStatus | 'ALL')[] = ['ALL', 'PENDING', 'APPROVED', 'ORDERED', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED'];
  paymentMethods = ['CASH', 'CARD', 'BANK_TRANSFER', 'ONLINE'];

  ngOnInit(): void {
    this.load();
    this.supplierService.getActive().subscribe({ next: s => this.suppliers.set(s) });
  }

  load(): void {
    this.loading.set(true);
    if (this.activeFilter() === 'ALL') {
      this.supplyService.getAll().subscribe({ next: res => { this.requests.set(res.content); this.loading.set(false); }, error: () => this.loading.set(false) });
    } else {
      this.supplyService.getByStatus(this.activeFilter() as SupplyRequestStatus).subscribe({
        next: res => { this.requests.set(res); this.loading.set(false); },
        error: () => this.loading.set(false)
      });
    }
  }

  setFilter(f: SupplyRequestStatus | 'ALL'): void { this.activeFilter.set(f); this.load(); }

  toggleExpand(id: number): void {
    if (this.expandedId() === id) {
      this.expandedId.set(null);
    } else {
      this.expandedId.set(id);
      this.addItemForm.reset({ itemName: '', quantity: 1, unitPrice: 0 });
      this.showPaymentForm.set(false);
      this.showInvoiceForm.set(false);
      this.loadPayments(id);
      this.loadInvoices(id);
    }
  }

  loadPayments(id: number): void {
    this.supplyService.getPayments(id).subscribe({
      next: p => this.payments.set(p),
      error: () => this.payments.set([])
    });
  }

  loadInvoices(id: number): void {
    this.supplyService.getInvoices(id).subscribe({
      next: inv => this.invoices.set(inv),
      error: () => this.invoices.set([])
    });
  }

  approve(id: number): void { this.supplyService.approve(id).subscribe({ next: () => this.load() }); }
  cancel(id: number): void { this.supplyService.cancel(id).subscribe({ next: () => this.load() }); }
  markOrdered(id: number): void { this.supplyService.markOrdered(id).subscribe({ next: () => this.load() }); }
  markInTransit(id: number): void { this.supplyService.markInTransit(id).subscribe({ next: () => this.load() }); }
  markDelivered(id: number): void { this.supplyService.markDelivered(id).subscribe({ next: () => this.load() }); }

  assignSupplier(id: number, event: Event): void {
    const supplierId = +(event.target as HTMLSelectElement).value;
    if (!supplierId) return;
    this.supplyService.assignSupplier(id, { supplierId }).subscribe({
      next: () => { this.toast.success('Поставщик назначен'); this.load(); }
    });
  }

  addItem(requestId: number): void {
    if (this.addItemForm.invalid) return;
    const v = this.addItemForm.getRawValue();
    this.supplyService.addItem(requestId, { itemName: v.itemName, quantity: v.quantity, unitPrice: v.unitPrice || undefined }).subscribe({
      next: () => { this.toast.success('Позиция добавлена'); this.addItemForm.reset({ itemName: '', quantity: 1, unitPrice: 0 }); this.load(); }
    });
  }

  deleteItem(requestId: number, itemId: number): void {
    this.supplyService.deleteItem(requestId, itemId).subscribe({
      next: () => { this.toast.success('Позиция удалена'); this.load(); }
    });
  }

  updateComment(requestId: number, event: Event): void {
    const comment = (event.target as HTMLInputElement).value;
    this.supplyService.updateComment(requestId, comment).subscribe({
      next: () => this.toast.success('Комментарий обновлён')
    });
  }

  addPayment(requestId: number): void {
    if (this.paymentForm.invalid) return;
    const v = this.paymentForm.getRawValue();
    this.supplyService.addPayment(requestId, { amount: v.amount, method: v.method, note: v.note || undefined }).subscribe({
      next: () => {
        this.toast.success('Оплата записана');
        this.paymentForm.reset({ amount: 0, method: 'CASH', note: '' });
        this.showPaymentForm.set(false);
        this.loadPayments(requestId);
      }
    });
  }

  addInvoice(requestId: number): void {
    if (this.invoiceForm.invalid) return;
    const v = this.invoiceForm.getRawValue();
    this.supplyService.addInvoice(requestId, { invoiceNumber: v.invoiceNumber, amount: v.amount, issuedAt: v.issuedAt }).subscribe({
      next: () => {
        this.toast.success('Счёт добавлен');
        this.invoiceForm.reset({ invoiceNumber: '', amount: 0, issuedAt: '' });
        this.showInvoiceForm.set(false);
        this.loadInvoices(requestId);
      }
    });
  }

  isEditable(statusName: string): boolean {
    return statusName !== 'DELIVERED' && statusName !== 'CANCELLED';
  }

  statusLabel(s: string): string {
    const map: Record<string, string> = { ALL: 'Все', NEW: 'Новая', PENDING: 'Ожидает', AUTO_FORMED: 'Авто', APPROVED: 'Одобрена', ORDERED: 'Заказана', IN_TRANSIT: 'В пути', DELIVERED: 'Доставлена', CANCELLED: 'Отменена' };
    return map[s] ?? s;
  }

  badgeClass(s: string): string {
    if (s === 'DELIVERED') return 'badge--success';
    if (s === 'CANCELLED') return 'badge--danger';
    if (s === 'APPROVED' || s === 'ORDERED') return 'badge--info';
    if (s === 'IN_TRANSIT') return 'badge--warning';
    return 'badge--muted';
  }

  paymentMethodLabel(m: string): string {
    const map: Record<string, string> = { CASH: 'Наличные', CARD: 'Карта', BANK_TRANSFER: 'Банк. перевод', ONLINE: 'Онлайн' };
    return map[m] ?? m;
  }
}
