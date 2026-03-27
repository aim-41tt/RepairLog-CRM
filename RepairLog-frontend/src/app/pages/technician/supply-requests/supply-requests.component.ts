import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { SupplyRequestService } from '../../../core/services/supply-request.service';
import { SupplyRequest, CreateSupplyRequestRequest } from '../../../core/models/supply-request.models';

@Component({
  selector: 'app-tech-supply-requests',
  standalone: true,
  imports: [PageLayoutComponent, ReactiveFormsModule, RouterLink, DatePipe],
  templateUrl: './supply-requests.component.html',
  styleUrl: './supply-requests.component.scss'
})
export class TechSupplyRequestsComponent implements OnInit {
  private supplyService = inject(SupplyRequestService);
  private fb = inject(FormBuilder);

  requests = signal<SupplyRequest[]>([]);
  loading = signal(false);
  showForm = signal(false);

  form = this.fb.nonNullable.group({
    itemName: ['', Validators.required],
    quantity: [1, [Validators.required, Validators.min(1)]],
    comment: ['']
  });

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.supplyService.myRequests().subscribe({
      next: r => { this.requests.set(r); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    const v = this.form.getRawValue();
    const req: CreateSupplyRequestRequest = {
      comment: v.comment || undefined,
      items: [{ itemName: v.itemName, quantity: v.quantity }]
    };
    this.supplyService.techCreate(req).subscribe({
      next: () => { this.showForm.set(false); this.form.reset({ itemName: '', quantity: 1, comment: '' }); this.load(); }
    });
  }

  statusLabel(s: string): string {
    const m: Record<string, string> = { DRAFT: 'Черновик', PENDING: 'Ожидает', APPROVED: 'Одобрена', ORDERED: 'Заказана', IN_TRANSIT: 'В пути', DELIVERED: 'Доставлена', CANCELLED: 'Отменена' };
    return m[s] ?? s;
  }
}
