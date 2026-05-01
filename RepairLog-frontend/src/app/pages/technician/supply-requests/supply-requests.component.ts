import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { SupplyRequestService } from '../../../core/services/supply-request.service';
import { ToastService } from '../../../core/services/toast.service';
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
  private toast = inject(ToastService);
  private fb = inject(FormBuilder);

  requests = signal<SupplyRequest[]>([]);
  loading = signal(false);
  showForm = signal(false);
  submitting = signal(false);

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
      error: (err) => { this.loading.set(false); this.toast.error(err?.error?.message ?? 'Не удалось загрузить заявки'); }
    });
  }

  submit(): void {
    if (this.form.invalid || this.submitting()) return;
    this.submitting.set(true);
    const v = this.form.getRawValue();
    const req: CreateSupplyRequestRequest = {
      comment: v.comment || undefined,
      items: [{ itemName: v.itemName, quantity: v.quantity }]
    };
    this.supplyService.techCreate(req).subscribe({
      next: () => {
        this.submitting.set(false);
        this.showForm.set(false);
        this.toast.success('Заявка создана');
        this.form.reset({ itemName: '', quantity: 1, comment: '' });
        this.load();
      },
      error: (err) => {
        this.submitting.set(false);
        this.toast.error(err?.error?.message ?? 'Не удалось создать заявку');
      }
    });
  }

  statusLabel(s: string): string {
    const m: Record<string, string> = {
      NEW: 'Новая', AUTO_FORMED: 'Авто-заявка',
      APPROVED: 'Одобрена', ORDERED: 'Заказана',
      IN_TRANSIT: 'В пути', PARTIALLY_DELIVERED: 'Частично получена',
      DELIVERED: 'Доставлена', CANCELLED: 'Отменена'
    };
    return m[s] ?? s;
  }
}
