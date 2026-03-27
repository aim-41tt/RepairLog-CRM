import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { InventoryService } from '../../../core/services/inventory.service';
import { InventoryItem, CreateInventoryRequest } from '../../../core/models/inventory.models';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [PageLayoutComponent, ReactiveFormsModule, RouterLink],
  templateUrl: './inventory.component.html',
  styleUrl: './inventory.component.scss'
})
export class InventoryComponent implements OnInit {
  private inventoryService = inject(InventoryService);
  private fb = inject(FormBuilder);

  items = signal<InventoryItem[]>([]);
  loading = signal(false);
  showForm = signal(false);
  showReceiveId = signal<number | null>(null);
  receiveQty = signal(1);

  form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    quantity: [0, [Validators.required, Validators.min(0)]],
    minQuantity: [5, Validators.required],
    purchasePrice: [0, Validators.required]
  });

  ngOnInit(): void { this.load(); }

  load(page = 0): void {
    this.loading.set(true);
    this.inventoryService.adminGetAll(page).subscribe({
      next: res => { this.items.set(res.content); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    const req: CreateInventoryRequest = this.form.getRawValue();
    this.inventoryService.adminCreate(req).subscribe({ next: () => { this.showForm.set(false); this.load(); } });
  }

  receive(itemId: number): void {
    this.inventoryService.adminReceive(itemId, { quantity: this.receiveQty() }).subscribe({ next: () => { this.showReceiveId.set(null); this.load(); } });
  }
}
