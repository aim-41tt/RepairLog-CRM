import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { InventoryService } from '../../../core/services/inventory.service';
import { ToastService } from '../../../core/services/toast.service';
import { InventoryItem } from '../../../core/models/inventory.models';

@Component({
  selector: 'app-warehouse',
  standalone: true,
  imports: [PageLayoutComponent, RouterLink],
  templateUrl: './warehouse.component.html',
  styleUrl: './warehouse.component.scss'
})
export class WarehouseComponent implements OnInit {
  private inventoryService = inject(InventoryService);
  private toast = inject(ToastService);

  items = signal<InventoryItem[]>([]);
  loading = signal(false);
  searchQuery = signal('');
  showLowStock = signal(false);

  ngOnInit(): void { this.loadAll(); }

  loadAll(): void {
    this.loading.set(true);
    this.inventoryService.getAll().subscribe({
      next: i => { this.items.set(i); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Не удалось загрузить материалы'); }
    });
  }

  search(): void {
    const q = this.searchQuery();
    if (!q.trim()) { this.loadAll(); return; }
    this.loading.set(true);
    this.inventoryService.search(q).subscribe({
      next: i => { this.items.set(i); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Ошибка поиска материалов'); }
    });
  }

  loadLowStock(): void {
    this.loading.set(true);
    this.inventoryService.getLowStock().subscribe({
      next: i => { this.items.set(i); this.loading.set(false); this.showLowStock.set(true); },
      error: () => {
        this.loading.set(false);
        this.showLowStock.set(false);
        this.toast.error('Не удалось загрузить материалы с низким запасом');
      }
    });
  }
}
