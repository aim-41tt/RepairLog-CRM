import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { AuditService } from '../../../core/services/audit.service';
import { AuditEntry } from '../../../core/models/audit.models';

@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [PageLayoutComponent, RouterLink, DatePipe, FormsModule],
  templateUrl: './audit-logs.component.html',
  styleUrl: './audit-logs.component.scss'
})
export class AuditLogsComponent implements OnInit {
  private auditService = inject(AuditService);

  entries = signal<AuditEntry[]>([]);
  loading = signal(false);
  page = signal(0);
  totalPages = signal(0);
  fromDate = signal('');
  toDate = signal('');

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.auditService.getAll(this.page()).subscribe({
      next: res => { this.entries.set(res.content); this.totalPages.set(res.totalPages); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  filterByPeriod(): void {
    if (!this.fromDate() || !this.toDate()) return;
    const fmt = (s: string) => s.length === 16 ? s + ':00' : s;
    this.loading.set(true);
    this.auditService.getByPeriod(fmt(this.fromDate()), fmt(this.toDate())).subscribe({
      next: res => { this.entries.set(res.content); this.totalPages.set(res.totalPages); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  resetFilter(): void {
    this.fromDate.set('');
    this.toDate.set('');
    this.page.set(0);
    this.load();
  }

  prev(): void { if (this.page() > 0) { this.page.update(p => p - 1); this.load(); } }
  next(): void { if (this.page() < this.totalPages() - 1) { this.page.update(p => p + 1); this.load(); } }
}
