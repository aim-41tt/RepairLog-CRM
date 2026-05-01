import { Component, inject, signal, Input, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { OrderService } from '../../../core/services/order.service';
import { DiagnosticService } from '../../../core/services/diagnostic.service';
import { WorkService } from '../../../core/services/work.service';
import { InventoryService } from '../../../core/services/inventory.service';
import { ReferenceService } from '../../../core/services/reference.service';
import { ReceiptService } from '../../../core/services/receipt.service';
import { ConfirmService } from '../../../core/services/confirm.service';
import { ToastService } from '../../../core/services/toast.service';
import { Order, StatusHistoryEntry } from '../../../core/models/order.models';
import { Diagnostic } from '../../../core/models/diagnostic.models';
import { WorkItem } from '../../../core/models/work.models';
import { InventoryItem } from '../../../core/models/inventory.models';
import { RepairStatus } from '../../../core/models/reference.models';
import { Receipt } from '../../../core/models/receipt.models';

type ActiveTab = 'info' | 'diagnostics' | 'works' | 'materials';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [PageLayoutComponent, ReactiveFormsModule, RouterLink, DatePipe],
  templateUrl: './order-detail.component.html',
  styleUrl: './order-detail.component.scss'
})
export class OrderDetailComponent implements OnInit {
  @Input() id!: string;

  private orderService = inject(OrderService);
  private diagnosticService = inject(DiagnosticService);
  private workService = inject(WorkService);
  private inventoryService = inject(InventoryService);
  private refService = inject(ReferenceService);
  private receiptService = inject(ReceiptService);
  private fb = inject(FormBuilder);
  private confirmService = inject(ConfirmService);
  private toast = inject(ToastService);

  order = signal<Order | null>(null);
  diagnostics = signal<Diagnostic[]>([]);
  works = signal<WorkItem[]>([]);
  inventory = signal<InventoryItem[]>([]);
  statuses = signal<RepairStatus[]>([]);
  receipt = signal<Receipt | null>(null);
  statusHistory = signal<StatusHistoryEntry[]>([]);
  activeTab = signal<ActiveTab>('info');
  loading = signal(false);
  editingDiagId = signal<number | null>(null);

  diagForm = this.fb.nonNullable.group({
    description: ['', Validators.required],
    solution: ['']
  });

  workForm = this.fb.nonNullable.group({
    description: ['', Validators.required],
    // B-07: add min(0) local validation so negative prices are rejected before hitting the server
    price: [0, [Validators.required, Validators.min(0)]]
  });

  inventorySearch = signal('');
  /** B-08: tracks whether an inventory search has been performed */
  inventorySearched = signal(false);
  consumeQty = signal(1);
  expandedInventoryId = signal<number | null>(null);

  ngOnInit(): void {
    const orderId = +this.id;
    this.loading.set(true);
    this.orderService.getOrderByIdTechnician(orderId).subscribe({
      next: o => { this.order.set(o); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Не удалось загрузить заказ'); }
    });
    this.diagnosticService.getByOrder(orderId).subscribe({
      next: d => this.diagnostics.set(d),
      error: () => this.diagnostics.set([])
    });
    this.refService.getRepairStatuses().subscribe({ next: s => this.statuses.set(s) });
    this.orderService.getStatusHistory(orderId, 'technician').subscribe({
      next: h => this.statusHistory.set(h),
      error: () => {}
    });
    this.receiptService.getByOrder(orderId, 'technician').subscribe({
      next: r => { this.receipt.set(r); this.works.set(r.works ?? []); },
      error: () => {}
    });
  }

  setTab(tab: ActiveTab): void {
    this.activeTab.set(tab);
    if (tab === 'materials' && !this.inventory().length) {
      this.inventoryService.getAll().subscribe({ next: i => this.inventory.set(i) });
    }
  }

  startEditDiag(d: Diagnostic): void {
    this.editingDiagId.set(d.id);
    this.diagForm.patchValue({ description: d.description, solution: d.solution ?? '' });
  }

  cancelEditDiag(): void {
    this.editingDiagId.set(null);
    this.diagForm.reset();
  }

  submitDiag(): void {
    if (this.diagForm.invalid) return;
    const v = this.diagForm.getRawValue();
    const editId = this.editingDiagId();

    if (editId !== null) {
      this.diagnosticService.update(editId, { description: v.description, solution: v.solution || undefined }).subscribe({
        next: d => {
          this.toast.success('Диагностика обновлена');
          this.diagnostics.update(arr => arr.map(item => item.id === editId ? d : item));
          this.editingDiagId.set(null);
          this.diagForm.reset();
        },
        error: (err) => this.toast.error(err?.error?.message ?? 'Не удалось обновить диагностику')
      });
    } else {
      this.diagnosticService.create({ repairOrderId: +this.id, description: v.description, solution: v.solution || undefined }).subscribe({
        next: d => { this.toast.success('Диагностика создана'); this.diagnostics.update(arr => [...arr, d]); this.diagForm.reset(); },
        error: (err) => this.toast.error(err?.error?.message ?? 'Не удалось создать диагностику')
      });
    }
  }

  submitWork(): void {
    if (this.workForm.invalid || !this.receipt()) return;
    const v = this.workForm.getRawValue();
    this.workService.create({ receiptId: this.receipt()!.id, description: v.description, price: v.price }).subscribe({
      next: () => {
        this.toast.success('Работа добавлена');
        this.workForm.reset({ description: '', price: 0 });
        this.receiptService.getByOrder(+this.id, 'technician').subscribe({
          next: r => { this.receipt.set(r); this.works.set(r.works ?? []); }
        });
      },
      error: (err) => {
        // B-07: map server-side 422 field errors back onto form controls so the UI shows them
        if (err?.status === 422 && err?.error?.fieldErrors?.length) {
          err.error.fieldErrors.forEach((fe: { field: string; message: string }) => {
            const ctrl = this.workForm.get(fe.field);
            if (ctrl) {
              ctrl.setErrors({ server: fe.message });
              ctrl.markAsTouched();
            }
          });
          this.toast.error('Проверьте введённые данные');
        } else {
          this.toast.error(err?.error?.message ?? 'Не удалось добавить работу');
        }
      }
    });
  }

  searchInventory(): void {
    const q = this.inventorySearch();
    if (!q.trim()) return;
    this.inventorySearched.set(true);
    this.inventoryService.search(q).subscribe({
      next: i => this.inventory.set(i),
      error: () => this.toast.error('Не удалось найти материалы')
    });
  }

  consume(item: InventoryItem): void {
    const qty = this.consumeQty();
    if (qty <= 0) { this.toast.error('Количество должно быть больше 0'); return; }
    if (!Number.isInteger(qty)) { this.toast.error('Количество должно быть целым числом'); return; }
    if (qty > item.quantity) { this.toast.error('Недостаточно на складе'); return; }
    this.confirmService.confirm({
      title: 'Списание материала',
      message: `Списать ${qty} шт. «${item.name}»?`,
      confirmLabel: 'Списать',
      variant: 'warning'
    }).subscribe(ok => {
      if (!ok) return;
      this.inventoryService.consume(item.id, qty, +this.id).subscribe({
        next: () => { this.toast.success('Материал списан'); this.searchInventory(); },
        error: (err) => this.toast.error(err?.error?.message ?? 'Не удалось списать материал')
      });
    });
  }

  changeStatus(statusId: number, select: HTMLSelectElement): void {
    const status = this.statuses().find(s => s.id === statusId);
    const name = status?.name ?? `#${statusId}`;
    const dangerStatuses = ['Отменен', 'Выдан'];
    this.confirmService.confirm({
      title: 'Смена статуса',
      message: `Изменить статус на «${name}»?`,
      confirmLabel: 'Изменить',
      variant: dangerStatuses.includes(name) ? 'danger' : 'warning'
    }).subscribe(ok => {
      if (!ok) { select.value = '0'; return; }
      this.orderService.updateStatusTechnician(+this.id, { statusId }).subscribe({
        next: () => {
          this.toast.success('Статус изменён');
          this.orderService.getOrderByIdTechnician(+this.id).subscribe({ next: o => this.order.set(o) });
          this.orderService.getStatusHistory(+this.id, 'technician').subscribe({ next: h => this.statusHistory.set(h), error: () => {} });
        },
        error: (err) => { select.value = '0'; this.toast.error(err?.error?.message ?? 'Не удалось изменить статус'); }
      });
    });
  }
}
