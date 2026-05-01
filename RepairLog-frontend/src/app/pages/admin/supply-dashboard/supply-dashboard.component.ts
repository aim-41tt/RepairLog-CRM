import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { SupplyRequestService } from '../../../core/services/supply-request.service';
import { ToastService } from '../../../core/services/toast.service';
import { SupplyDashboardResponse } from '../../../core/models/supply-request.models';

@Component({
  selector: 'app-supply-dashboard',
  standalone: true,
  imports: [PageLayoutComponent, RouterLink, DecimalPipe],
  templateUrl: './supply-dashboard.component.html',
  styleUrl: './supply-dashboard.component.scss'
})
export class SupplyDashboardComponent implements OnInit {
  private supplyService = inject(SupplyRequestService);
  private toast = inject(ToastService);

  dashboard = signal<SupplyDashboardResponse | null>(null);
  loading = signal(false);

  ngOnInit(): void {
    this.loading.set(true);
    this.supplyService.getDashboard().subscribe({
      next: d => { this.dashboard.set(d); this.loading.set(false); },
      error: (err) => {
        this.loading.set(false);
        this.toast.error(err?.error?.message ?? 'Не удалось загрузить дашборд снабжения');
      }
    });
  }
}
