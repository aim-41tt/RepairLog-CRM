import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { API_URL } from '../../../core/tokens/api-url.token';
import { Client } from '../../../core/models/client.models';

@Component({
  selector: 'app-clients-anonymize',
  standalone: true,
  imports: [PageLayoutComponent, RouterLink, FormsModule, DatePipe],
  templateUrl: './clients-anonymize.component.html',
  styleUrl: './clients-anonymize.component.scss'
})
export class ClientsAnonymizeComponent {
  private http = inject(HttpClient);
  private base = `${inject(API_URL)}/admin/clients`;

  query = signal('');
  clients = signal<Client[]>([]);
  loading = signal(false);
  message = signal('');
  messageOk = signal(true);
  confirmId = signal<number | null>(null);

  search(): void {
    if (!this.query().trim()) return;
    this.loading.set(true);
    this.message.set('');
    this.http.get<Client[]>(`${this.base}/search`, { params: { query: this.query() } }).subscribe({
      next: res => { this.clients.set(res); this.loading.set(false); },
      error: () => { this.loading.set(false); this.message.set('Ошибка поиска'); this.messageOk.set(false); }
    });
  }

  requestAnonymize(id: number): void {
    this.confirmId.set(id);
  }

  cancelConfirm(): void {
    this.confirmId.set(null);
  }

  confirmAnonymize(): void {
    const id = this.confirmId();
    if (id === null) return;
    this.http.post<{ message: string }>(`${this.base}/${id}/anonymize`, {}).subscribe({
      next: res => {
        this.message.set(res.message);
        this.messageOk.set(true);
        this.confirmId.set(null);
        this.search();
      },
      error: err => {
        this.message.set(err.error?.message ?? 'Ошибка анонимизации');
        this.messageOk.set(false);
        this.confirmId.set(null);
      }
    });
  }

  isAnonymized(c: Client): boolean {
    return c.name === 'Удалён' && c.surname === 'Удалён';
  }
}
