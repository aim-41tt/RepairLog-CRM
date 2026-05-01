import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { AuditService } from '../../../core/services/audit.service';
import { ToastService } from '../../../core/services/toast.service';
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
  private toast = inject(ToastService);

  entries = signal<AuditEntry[]>([]);
  loading = signal(false);
  page = signal(0);
  totalPages = signal(0);
  fromDate = signal('');
  toDate = signal('');

  private RESOURCE_LABELS: Record<string, string> = {
    CLIENT: 'Клиент', DEVICE: 'Устройство', REPAIR_ORDER: 'Заказ',
    EMPLOYEE: 'Сотрудник', DOCUMENT: 'Документ (ПДн)', RECEIPT: 'Чек',
  };
  private EVENT_LABELS: Record<string, string> = {
    LOGIN: 'Вход в систему', LOGIN_FAILED: 'Вход (неудача)',
    LOGOUT: 'Выход', PASSWORD_CHANGE: 'Учётная запись',
    SESSION_TERMINATED: 'Сессия', DATA_ACCESS: 'Доступ к данным',
    DATA_CREATE: 'Создание данных', DATA_UPDATE: 'Обновление данных',
    DATA_DELETE: 'Удаление данных', ACCESS_DENIED: 'Отказ в доступе',
  };

  /** P3: localization for action field values from the backend audit log */
  private ACTION_LABELS: Record<string, string> = {
    CREATE: 'Создание', UPDATE: 'Обновление', DELETE: 'Удаление',
    LOGIN: 'Вход', LOGOUT: 'Выход', EXPORT: 'Экспорт',
    CONSENT_GIVEN: 'Согласие получено', CONSENT_REVOKED: 'Согласие отозвано',
    ANONYMIZE: 'Обезличивание', VIEW: 'Просмотр', SEARCH: 'Поиск',
  };

  actionLabel(action: string | undefined): string {
    if (!action) return '—';
    return this.ACTION_LABELS[action] ?? action;
  }

  resourceLabel(e: AuditEntry): string {
    if (e.resourceType) {
      const label = this.RESOURCE_LABELS[e.resourceType] ?? e.resourceType;
      return e.resourceId ? `${label} #${e.resourceId}` : label;
    }
    return e.eventType ? (this.EVENT_LABELS[e.eventType] ?? '—') : '—';
  }

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.auditService.getAll(this.page()).subscribe({
      next: res => { this.entries.set(res.content); this.totalPages.set(res.totalPages); this.loading.set(false); },
      error: (err) => { this.loading.set(false); this.toast.error(err?.error?.message ?? 'Не удалось загрузить журнал аудита'); }
    });
  }

  filterByPeriod(): void {
    if (!this.fromDate() || !this.toDate()) return;
    const fmt = (s: string) => s.length === 16 ? s + ':00' : s;
    this.loading.set(true);
    this.auditService.getByPeriod(fmt(this.fromDate()), fmt(this.toDate())).subscribe({
      next: res => { this.entries.set(res.content); this.totalPages.set(res.totalPages); this.loading.set(false); },
      error: (err) => { this.loading.set(false); this.toast.error(err?.error?.message ?? 'Не удалось загрузить журнал за период'); }
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
