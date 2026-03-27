import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { ClientService } from '../../../core/services/client.service';
import { Client, CreateClientRequest } from '../../../core/models/client.models';

@Component({
  selector: 'app-clients',
  standalone: true,
  imports: [PageLayoutComponent, ReactiveFormsModule, RouterLink],
  templateUrl: './clients.component.html',
  styleUrl: './clients.component.scss'
})
export class ClientsComponent {
  private clientService = inject(ClientService);
  private fb = inject(FormBuilder);

  clients = signal<Client[]>([]);
  selected = signal<Client | null>(null);
  showForm = signal(false);
  editingId = signal<number | null>(null);
  searchQuery = signal('');

  form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    surname: ['', Validators.required],
    patronymic: [''],
    dateBirth: ['', Validators.required],
    phone: ['', Validators.required],
    email: [''],
    consentGiven: [false],
    notificationsEnabled: [false]
  });

  search(): void {
    const q = this.searchQuery();
    if (!q.trim()) return;
    this.clientService.search(q).subscribe({ next: r => this.clients.set(r) });
  }

  openCreate(): void {
    this.editingId.set(null);
    this.form.reset();
    this.showForm.set(true);
  }

  openEdit(c: Client): void {
    this.editingId.set(c.id);
    this.form.patchValue({
      name: c.name, surname: c.surname, patronymic: c.patronymic ?? '',
      dateBirth: c.dateBirth ?? '', phone: c.phone, email: c.email ?? '',
      consentGiven: c.consentGiven, notificationsEnabled: c.notificationsEnabled
    });
    this.showForm.set(true);
  }

  submit(): void {
    if (this.form.invalid) return;
    const req: CreateClientRequest = this.form.getRawValue();
    const id = this.editingId();
    const obs = id ? this.clientService.update(id, req) : this.clientService.create(req);
    obs.subscribe({ next: () => { this.showForm.set(false); this.search(); } });
  }

  giveConsent(id: number): void {
    this.clientService.giveConsent(id).subscribe({ next: () => this.search() });
  }

  formatPhone(event: Event): void {
    const input = event.target as HTMLInputElement;
    let digits = input.value.replace(/\D/g, '');
    if (digits.startsWith('8')) digits = '7' + digits.slice(1);
    if (!digits.startsWith('7') && digits.length > 0) digits = '7' + digits;
    let formatted = '';
    if (digits.length > 0) formatted = '+' + digits[0];
    if (digits.length > 1) formatted += ' (' + digits.slice(1, 4);
    if (digits.length >= 4) formatted += ')';
    if (digits.length > 4) formatted += ' ' + digits.slice(4, 7);
    if (digits.length > 7) formatted += '-' + digits.slice(7, 9);
    if (digits.length > 9) formatted += '-' + digits.slice(9, 11);
    input.value = formatted;
    this.form.get('phone')?.setValue(formatted, { emitEvent: false });
  }
}
