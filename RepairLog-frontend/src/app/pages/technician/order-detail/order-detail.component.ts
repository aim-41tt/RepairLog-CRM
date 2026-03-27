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
    price: [0, Validators.required]
  });

  inventorySearch = signal('');
  consumeQty = signal(1);

  ngOnInit(): void {
    const orderId = +this.id;
    this.loading.set(true);
    this.orderService.getOrderByIdTechnician(orderId).subscribe({ next: o => { this.order.set(o); this.loading.set(false); } });
    this.diagnosticService.getByOrder(orderId).subscribe({ next: d => this.diagnostics.set(d) });
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
          this.diagnostics.update(arr => arr.map(item => item.id === editId ? d : item));
          this.editingDiagId.set(null);
          this.diagForm.reset();
        }
      });
    } else {
      this.diagnosticService.create({ repairOrderId: +this.id, description: v.description, solution: v.solution || undefined }).subscribe({
        next: d => { this.diagnostics.update(arr => [...arr, d]); this.diagForm.reset(); }
      });
    }
  }

  submitWork(): void {
    if (this.workForm.invalid || !this.receipt()) return;
    const v = this.workForm.getRawValue();
    this.workService.create({ receiptId: this.receipt()!.id, description: v.description, price: v.price }).subscribe({
      next: () => {
        this.workForm.reset();
        this.receiptService.getByOrder(+this.id, 'technician').subscribe({
          next: r => { this.receipt.set(r); this.works.set(r.works ?? []); }
        });
      }
    });
  }

  searchInventory(): void {
    const q = this.inventorySearch();
    if (!q.trim()) return;
    this.inventoryService.search(q).subscribe({ next: i => this.inventory.set(i) });
  }

  consume(item: InventoryItem): void {
    const qty = this.consumeQty();
    this.confirmService.confirm({
      title: 'Списание материала',
      message: `Списать ${qty} шт. «${item.name}»?`,
      confirmLabel: 'Списать',
      variant: 'warning'
    }).subscribe(ok => {
      if (!ok) return;
      this.inventoryService.consume(item.id, qty, +this.id).subscribe({ next: () => this.searchInventory() });
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
          this.orderService.getOrderByIdTechnician(+this.id).subscribe({ next: o => this.order.set(o) });
          this.orderService.getStatusHistory(+this.id, 'technician').subscribe({ next: h => this.statusHistory.set(h), error: () => {} });
        }
      });
    });
  }
}
