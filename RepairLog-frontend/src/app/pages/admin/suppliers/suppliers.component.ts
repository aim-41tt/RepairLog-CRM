import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { SupplierService } from '../../../core/services/supplier.service';
import { ToastService } from '../../../core/services/toast.service';
import { Supplier, CreateSupplierRequest } from '../../../core/models/supplier.models';

@Component({
  selector: 'app-suppliers',
  standalone: true,
  imports: [PageLayoutComponent, ReactiveFormsModule, RouterLink],
  templateUrl: './suppliers.component.html',
  styleUrl: './suppliers.component.scss'
})
export class SuppliersComponent implements OnInit {
  private supplierService = inject(SupplierService);
  private toast = inject(ToastService);
  private fb = inject(FormBuilder);

  suppliers = signal<Supplier[]>([]);
  loading = signal(false);
  showForm = signal(false);
  activeFilter = signal<string>('ALL');
  showActiveOnly = signal(false);

  integrationTypes = ['ALL', 'MANUAL', 'API', 'EMAIL', 'EDI'];

  form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    contactPerson: [''],
    phone: [''],
    email: [''],
    integrationType: ['MANUAL']
  });

  ngOnInit(): void { this.load(); }

  load(page = 0): void {
    this.loading.set(true);
    if (this.activeFilter() === 'ALL') {
      this.supplierService.getAll(page).subscribe({
        next: res => { this.suppliers.set(res.content); this.loading.set(false); },
        error: () => this.loading.set(false)
      });
    } else {
      this.supplierService.getByIntegrationType(this.activeFilter()).subscribe({
        next: res => { this.suppliers.set(res); this.loading.set(false); },
        error: () => this.loading.set(false)
      });
    }
  }

  setFilter(type: string): void {
    this.activeFilter.set(type);
    this.load();
  }

  submit(): void {
    if (this.form.invalid) return;
    const req: CreateSupplierRequest = this.form.getRawValue();
    this.supplierService.create(req).subscribe({
      next: () => { this.showForm.set(false); this.toast.success('Поставщик создан'); this.load(); }
    });
  }

  toggleActive(s: Supplier): void {
    this.supplierService.toggleActive(s.id, !s.active).subscribe({ next: () => this.load() });
  }

  toggleActiveFilter(): void {
    this.showActiveOnly.update(v => !v);
    if (this.showActiveOnly()) {
      this.loading.set(true);
      this.supplierService.getActive().subscribe({
        next: res => { this.suppliers.set(res); this.loading.set(false); },
        error: () => this.loading.set(false)
      });
    } else {
      this.load();
    }
  }

  integrationLabel(type: string): string {
    const map: Record<string, string> = { ALL: 'Все', MANUAL: 'Ручной', API: 'API', EMAIL: 'Email', EDI: 'EDI' };
    return map[type] ?? type;
  }
}
