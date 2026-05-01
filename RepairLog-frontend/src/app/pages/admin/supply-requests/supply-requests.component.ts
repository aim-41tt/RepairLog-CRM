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
import { ConfirmService } from '../../../core/services/confirm.service';

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
  private confirm = inject(ConfirmService);
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
    paidAmount: [0, [Validators.required, Validators.min(0.01)]],
    paymentMethod: ['CASH', Validators.required],
    comment: ['']
  });

  // Invoice form — dueDate must be >= invoiceDate when present
  invoiceForm = this.fb.nonNullable.group({
    invoiceNumber: ['', Validators.required],
    totalAmount: [0, [Validators.required, Validators.min(0.01)]],
    invoiceDate: ['', Validators.required],
    dueDate: ['']
  }, { validators: (group) => {
    const inv = group.get('invoiceDate')?.value;
    const due = group.get('dueDate')?.value;
    if (!inv || !due) return null;
    return new Date(due) >= new Date(inv) ? null : { dueBeforeInvoice: true };
  } });

  statuses: (SupplyRequestStatus | 'ALL')[] = ['ALL', 'NEW', 'AUTO_FORMED', 'APPROVED', 'ORDERED', 'IN_TRANSIT', 'PARTIALLY_DELIVERED', 'DELIVERED', 'CANCELLED'];
  paymentMethods = ['CASH', 'CARD', 'BANK_TRANSFER', 'TRANSFER', 'INVOICE', 'OTHER'];

  ngOnInit(): void {
    this.load();
    this.supplierService.getActive().subscribe({
      next: s => this.suppliers.set(s),
      error: () => { this.suppliers.set([]); this.toast.error('Не удалось загрузить поставщиков'); }
    });
  }

  load(): void {
    this.loading.set(true);
    this.expandedId.set(null);
    if (this.activeFilter() === 'ALL') {
      this.supplyService.getAll().subscribe({
        next: res => { this.requests.set(res.content); this.loading.set(false); },
        error: () => { this.loading.set(false); this.toast.error('Не удалось загрузить заявки'); }
      });
    } else {
      this.supplyService.getByStatus(this.activeFilter() as SupplyRequestStatus).subscribe({
        next: res => { this.requests.set(res); this.loading.set(false); },
        error: () => { this.loading.set(false); this.toast.error('Не удалось загрузить заявки'); }
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

  private errMsg(err: any, fallback: string): string {
    return err?.error?.message ?? fallback;
  }

  approve(id: number): void {
    this.confirm.confirm({
      title: 'Одобрить заявку',
      message: 'Вы уверены, что хотите одобрить эту заявку на поставку?',
      confirmLabel: 'Одобрить',
      variant: 'info'
    }).subscribe(ok => {
      if (!ok) return;
      this.supplyService.approve(id).subscribe({
        next: () => { this.toast.success('Заявка одобрена'); this.load(); },
        error: (err) => this.toast.error(this.errMsg(err, 'Не удалось одобрить заявку'))
      });
    });
  }

  cancel(id: number): void {
    this.confirm.confirm({
      title: 'Отменить заявку',
      message: 'Вы уверены, что хотите отменить эту заявку? Действие необратимо.',
      confirmLabel: 'Отменить',
      variant: 'danger'
    }).subscribe(ok => {
      if (!ok) return;
      this.supplyService.cancel(id).subscribe({
        next: () => { this.toast.success('Заявка отменена'); this.load(); },
        error: (err) => this.toast.error(this.errMsg(err, 'Не удалось отменить заявку'))
      });
    });
  }
  markOrdered(id: number): void {
    this.supplyService.markOrdered(id).subscribe({
      next: () => { this.toast.success('Помечено как заказано'); this.load(); },
      error: (err) => this.toast.error(this.errMsg(err, 'Ошибка операции'))
    });
  }
  markInTransit(id: number): void {
    this.supplyService.markInTransit(id).subscribe({
      next: () => { this.toast.success('Помечено как в пути'); this.load(); },
      error: (err) => this.toast.error(this.errMsg(err, 'Ошибка операции'))
    });
  }
  markDelivered(id: number): void {
    this.supplyService.markDelivered(id).subscribe({
      next: () => { this.toast.success('Помечено как доставлено'); this.load(); },
      error: (err) => this.toast.error(this.errMsg(err, 'Ошибка операции'))
    });
  }

  assignSupplier(id: number, event: Event): void {
    const supplierId = +(event.target as HTMLSelectElement).value;
    if (!supplierId) return;
    this.supplyService.assignSupplier(id, { supplierId }).subscribe({
      next: () => { this.toast.success('Поставщик назначен'); this.load(); },
      error: (err) => this.toast.error(this.errMsg(err, 'Не удалось назначить поставщика'))
    });
  }

  addItem(requestId: number): void {
    if (this.addItemForm.invalid) return;
    const v = this.addItemForm.getRawValue();
    this.supplyService.addItem(requestId, { itemName: v.itemName, quantity: v.quantity, unitPrice: v.unitPrice || undefined }).subscribe({
      next: () => { this.toast.success('Позиция добавлена'); this.addItemForm.reset({ itemName: '', quantity: 1, unitPrice: 0 }); this.load(); },
      error: (err) => this.toast.error(this.errMsg(err, 'Не удалось добавить позицию'))
    });
  }

  deleteItem(requestId: number, itemId: number): void {
    this.supplyService.deleteItem(requestId, itemId).subscribe({
      next: () => { this.toast.success('Позиция удалена'); this.load(); },
      error: (err) => this.toast.error(this.errMsg(err, 'Не удалось удалить позицию'))
    });
  }

  updateComment(requestId: number, event: Event): void {
    const comment = (event.target as HTMLInputElement).value;
    this.supplyService.updateComment(requestId, comment).subscribe({
      next: () => this.toast.success('Комментарий обновлён'),
      error: (err) => this.toast.error(this.errMsg(err, 'Не удалось обновить комментарий'))
    });
  }

  addPayment(requestId: number): void {
    if (this.paymentForm.invalid) return;
    const v = this.paymentForm.getRawValue();
    this.supplyService.addPayment(requestId, { paidAmount: v.paidAmount, paymentMethod: v.paymentMethod, comment: v.comment || undefined }).subscribe({
      next: () => {
        this.toast.success('Оплата записана');
        this.paymentForm.reset({ paidAmount: 0, paymentMethod: 'CASH', comment: '' });
        this.showPaymentForm.set(false);
        this.loadPayments(requestId);
      },
      error: (err) => this.toast.error(this.errMsg(err, 'Не удалось записать оплату'))
    });
  }

  addInvoice(requestId: number): void {
    if (this.invoiceForm.invalid) return;
    const v = this.invoiceForm.getRawValue();
    this.supplyService.addInvoice(requestId, {
      invoiceNumber: v.invoiceNumber,
      totalAmount: v.totalAmount,
      invoiceDate: v.invoiceDate,
      dueDate: v.dueDate || undefined
    }).subscribe({
      next: () => {
        this.toast.success('Счёт добавлен');
        this.invoiceForm.reset({ invoiceNumber: '', totalAmount: 0, invoiceDate: '', dueDate: '' });
        this.showInvoiceForm.set(false);
        this.loadInvoices(requestId);
      },
      error: (err) => this.toast.error(this.errMsg(err, 'Не удалось добавить счёт'))
    });
  }

  isEditable(statusName: string): boolean {
    return statusName !== 'DELIVERED' && statusName !== 'PARTIALLY_DELIVERED' && statusName !== 'CANCELLED';
  }

  statusLabel(s: string): string {
    const map: Record<string, string> = {
      ALL: 'Все', NEW: 'Новая', AUTO_FORMED: 'Авто-заявка',
      APPROVED: 'Одобрена', ORDERED: 'Заказана',
      IN_TRANSIT: 'В пути', PARTIALLY_DELIVERED: 'Частично получена',
      DELIVERED: 'Доставлена', CANCELLED: 'Отменена'
    };
    return map[s] ?? s;
  }

  badgeClass(s: string): string {
    if (s === 'DELIVERED') return 'badge--success';
    if (s === 'PARTIALLY_DELIVERED') return 'badge--warning';
    if (s === 'CANCELLED') return 'badge--danger';
    if (s === 'APPROVED' || s === 'ORDERED') return 'badge--info';
    if (s === 'IN_TRANSIT') return 'badge--warning';
    if (s === 'NEW' || s === 'AUTO_FORMED') return 'badge--muted';
    return 'badge--muted';
  }

  paymentMethodLabel(m: string): string {
    const map: Record<string, string> = {
      CASH: 'Наличные', CARD: 'Карта',
      BANK_TRANSFER: 'Банк. поручение', TRANSFER: 'Банк. перевод',
      INVOICE: 'По счёту', OTHER: 'Другое'
    };
    return map[m] ?? m;
  }
}
