import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { InventoryService } from '../../../core/services/inventory.service';
import { ToastService } from '../../../core/services/toast.service';
import { InventoryItem, CreateInventoryRequest } from '../../../core/models/inventory.models';
import { ConfirmService } from '../../../core/services/confirm.service';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [PageLayoutComponent, ReactiveFormsModule, RouterLink, DatePipe],
  templateUrl: './inventory.component.html',
  styleUrl: './inventory.component.scss'
})
export class InventoryComponent implements OnInit {
  private inventoryService = inject(InventoryService);
  private toast = inject(ToastService);
  private confirm = inject(ConfirmService);
  private fb = inject(FormBuilder);

  items = signal<InventoryItem[]>([]);
  loading = signal(false);
  showForm = signal(false);
  showReceiveId = signal<number | null>(null);
  receiveQty = signal(1);
  receiveComment = signal('');
  expandedItemId = signal<number | null>(null);
  page = signal(0);
  totalPages = signal(0);

  form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    partNumber: [''],
    description: [''],
    quantity: [0, [Validators.required, Validators.min(0)]],
    minQuantity: [5, [Validators.required, Validators.min(0)]],
    purchasePrice: [0, [Validators.required, Validators.min(0)]],
    sellingPrice: [0, [Validators.required, Validators.min(0)]]
  });

  ngOnInit(): void { this.load(); }

  load(page = 0): void {
    this.loading.set(true);
    this.page.set(page);
    this.inventoryService.adminGetAll(page).subscribe({
      next: res => { this.items.set(res.content); this.totalPages.set(res.totalPages); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Не удалось загрузить склад'); }
    });
  }

  prev(): void { if (this.page() > 0) this.load(this.page() - 1); }
  next(): void { if (this.page() < this.totalPages() - 1) this.load(this.page() + 1); }

  submit(): void {
    if (this.form.invalid) return;
    const v = this.form.getRawValue();
    const req: CreateInventoryRequest = {
      name: v.name,
      partNumber: v.partNumber || undefined,
      description: v.description || undefined,
      quantity: v.quantity,
      minQuantity: v.minQuantity,
      purchasePrice: v.purchasePrice,
      sellingPrice: v.sellingPrice
    };
    this.inventoryService.adminCreate(req).subscribe({
      next: () => {
        this.toast.success('Позиция создана');
        this.form.reset({ name: '', partNumber: '', description: '', quantity: 0, minQuantity: 5, purchasePrice: 0, sellingPrice: 0 });
        this.showForm.set(false);
        this.load();
      },
      error: (err) => this.toast.error(err?.error?.message ?? 'Не удалось создать позицию')
    });
  }

  toggleExpand(itemId: number): void {
    this.expandedItemId.set(this.expandedItemId() === itemId ? null : itemId);
  }

  deleteItem(item: InventoryItem): void {
    this.confirm.confirm({
      title: 'Удалить позицию',
      message: `Удалить «${item.name}» со склада? Действие необратимо.`,
      confirmLabel: 'Удалить',
      variant: 'danger'
    }).subscribe(ok => {
      if (!ok) return;
      this.inventoryService.adminDelete(item.id).subscribe({
        next: () => { this.toast.success('Позиция удалена'); this.load(); },
        error: (err) => this.toast.error(err?.error?.message ?? 'Не удалось удалить позицию')
      });
    });
  }

  receive(itemId: number): void {
    if (this.receiveQty() <= 0) { this.toast.error('Количество должно быть больше 0'); return; }
    this.inventoryService.adminReceive(itemId, { quantity: this.receiveQty(), comment: this.receiveComment() || undefined }).subscribe({
      next: () => {
        this.toast.success('Товар принят');
        this.showReceiveId.set(null);
        this.receiveQty.set(1);
        this.receiveComment.set('');
        this.load();
      },
      error: (err) => this.toast.error(err?.error?.message ?? 'Не удалось принять товар')
    });
  }
}
