import { Component, inject, signal, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { OrderService } from '../../../core/services/order.service';
import { ToastService } from '../../../core/services/toast.service';
import { Order } from '../../../core/models/order.models';

@Component({
  selector: 'app-unassigned-orders',
  standalone: true,
  imports: [PageLayoutComponent, RouterLink],
  templateUrl: './unassigned-orders.component.html',
  styleUrl: './unassigned-orders.component.scss'
})
export class UnassignedOrdersComponent implements OnInit {
  private orderService = inject(OrderService);
  private router = inject(Router);
  private toast = inject(ToastService);

  orders = signal<Order[]>([]);
  loading = signal(false);

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.orderService.getUnassigned().subscribe({
      next: r => { this.orders.set(r); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Не удалось загрузить заказы'); }
    });
  }

  take(order: Order): void {
    this.orderService.takeOrder(order.id).subscribe({
      next: () => { this.toast.success('Заказ взят в работу'); this.router.navigate(['/technician/orders', order.id]); },
      error: (err) => this.toast.error(err?.error?.message ?? 'Не удалось взять заказ')
    });
  }
}
