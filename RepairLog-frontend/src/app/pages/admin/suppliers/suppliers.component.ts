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

  page = signal(0);
  totalPages = signal(0);

  form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(200)]],
    contactPerson: ['', Validators.maxLength(100)],
    phone: ['', Validators.maxLength(20)],
    email: ['', [Validators.email, Validators.maxLength(200)]],
    address: ['', Validators.maxLength(500)],
    inn: ['', [Validators.maxLength(12), Validators.pattern(/^(\d{10}|\d{12})?$/)]],
    integrationType: ['MANUAL'],
    priceSource: [''],
    orderMethod: [''],
    websiteUrl: ['', Validators.pattern(/^(https?:\/\/.+)?$/)],
    contactMessenger: [''],
    priceListEmail: ['', [Validators.email, Validators.maxLength(200)]],
    externalSupplierId: ['', Validators.maxLength(100)]
  });

  private readonly emptyForm: { [k: string]: any } = {
    name: '', contactPerson: '', phone: '', email: '', address: '', inn: '',
    integrationType: 'MANUAL', priceSource: '', orderMethod: '', websiteUrl: '',
    contactMessenger: '', priceListEmail: '', externalSupplierId: ''
  };

  ngOnInit(): void { this.load(); }

  load(page = 0): void {
    this.loading.set(true);
    this.page.set(page);
    if (this.activeFilter() === 'ALL') {
      this.supplierService.getAll(page).subscribe({
        next: res => { this.suppliers.set(res.content); this.totalPages.set(res.totalPages); this.loading.set(false); },
        error: () => { this.loading.set(false); this.toast.error('Не удалось загрузить поставщиков'); }
      });
    } else {
      this.supplierService.getByIntegrationType(this.activeFilter()).subscribe({
        next: res => { this.suppliers.set(res); this.totalPages.set(0); this.loading.set(false); },
        error: () => { this.loading.set(false); this.toast.error('Не удалось загрузить поставщиков'); }
      });
    }
  }

  setFilter(type: string): void {
    this.activeFilter.set(type);
    this.page.set(0);
    this.load(0);
  }

  prev(): void { if (this.page() > 0) this.load(this.page() - 1); }
  next(): void { if (this.page() < this.totalPages() - 1) this.load(this.page() + 1); }

  submit(): void {
    if (this.form.invalid) return;
    const v = this.form.getRawValue();
    const req: CreateSupplierRequest = {
      name: v.name,
      contactPerson: v.contactPerson || undefined,
      phone: v.phone || undefined,
      email: v.email || undefined,
      address: v.address || undefined,
      inn: v.inn || undefined,
      integrationType: v.integrationType || undefined,
      priceSource: v.priceSource || undefined,
      orderMethod: v.orderMethod || undefined,
      websiteUrl: v.websiteUrl || undefined,
      contactMessenger: v.contactMessenger || undefined,
      priceListEmail: v.priceListEmail || undefined,
      externalSupplierId: v.externalSupplierId || undefined
    };
    this.supplierService.create(req).subscribe({
      next: () => {
        this.showForm.set(false);
        this.toast.success('Поставщик создан');
        this.form.reset(this.emptyForm);
        this.load(0);
      },
      error: (err) => this.toast.error(err?.error?.message ?? 'Не удалось создать поставщика')
    });
  }

  toggleActive(s: Supplier): void {
    this.supplierService.toggleActive(s.id, !s.active).subscribe({
      next: () => { this.toast.success(s.active ? 'Поставщик деактивирован' : 'Поставщик активирован'); this.load(); },
      error: (err) => this.toast.error(err?.error?.message ?? 'Не удалось изменить статус')
    });
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
