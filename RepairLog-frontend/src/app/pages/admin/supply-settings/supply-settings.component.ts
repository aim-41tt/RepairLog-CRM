import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { SupplyRequestService } from '../../../core/services/supply-request.service';
import { ToastService } from '../../../core/services/toast.service';
import { SupplySettingResponse } from '../../../core/models/supply-request.models';

@Component({
  selector: 'app-supply-settings',
  standalone: true,
  imports: [PageLayoutComponent, RouterLink],
  templateUrl: './supply-settings.component.html',
  styleUrl: './supply-settings.component.scss'
})
export class SupplySettingsComponent implements OnInit {
  private supplyService = inject(SupplyRequestService);
  private toast = inject(ToastService);

  settings = signal<SupplySettingResponse[]>([]);
  loading = signal(false);
  editingKey = signal<string | null>(null);
  editValue = signal('');

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.supplyService.getSettings().subscribe({
      next: s => { this.settings.set(s); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  startEdit(setting: SupplySettingResponse): void {
    this.editingKey.set(setting.settingKey);
    this.editValue.set(setting.settingValue);
  }

  cancelEdit(): void {
    this.editingKey.set(null);
    this.editValue.set('');
  }

  saveEdit(key: string): void {
    this.supplyService.updateSetting(key, this.editValue()).subscribe({
      next: () => {
        this.toast.success('Настройка обновлена');
        this.editingKey.set(null);
        this.load();
      }
    });
  }

  onValueChange(event: Event): void {
    this.editValue.set((event.target as HTMLInputElement).value);
  }

  settingLabel(key: string): string {
    const map: Record<string, string> = {
      'auto_order_threshold': 'Порог автозаказа',
      'low_stock_threshold': 'Порог низкого запаса',
      'default_supplier_id': 'Поставщик по умолчанию',
      'auto_approve_enabled': 'Авто-одобрение',
      'notification_email': 'Email для уведомлений',
      'reorder_lead_days': 'Дней на доставку'
    };
    return map[key] ?? key;
  }
}
