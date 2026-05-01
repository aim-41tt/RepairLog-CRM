import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { ClientService } from '../../../core/services/client.service';
import { ToastService } from '../../../core/services/toast.service';
import { Client, CreateClientRequest } from '../../../core/models/client.models';

/** B-13: validates that date is not in the future */
function pastOrPresentValidator(control: AbstractControl): ValidationErrors | null {
  if (!control.value) return null;
  const entered = new Date(control.value);
  const today = new Date();
  today.setHours(23, 59, 59, 999);
  return entered > today ? { futureDate: true } : null;
}

/** B-12: phone number basic format validator */
function phoneValidator(control: AbstractControl): ValidationErrors | null {
  if (!control.value) return null;
  const clean = (control.value as string).replace(/[\s()\-+]/g, '');
  return /^\d{7,15}$/.test(clean) ? null : { invalidPhone: true };
}

@Component({
  selector: 'app-clients',
  standalone: true,
  imports: [PageLayoutComponent, ReactiveFormsModule, RouterLink],
  templateUrl: './clients.component.html',
  styleUrl: './clients.component.scss'
})
export class ClientsComponent {
  private clientService = inject(ClientService);
  private toast = inject(ToastService);
  private fb = inject(FormBuilder);

  clients = signal<Client[]>([]);
  selected = signal<Client | null>(null);
  showForm = signal(false);
  editingId = signal<number | null>(null);
  searchQuery = signal('');
  /** B-08: tracks whether a search has been attempted (to distinguish "no results" vs "not searched yet") */
  searched = signal(false);

  readonly todayIso = new Date().toISOString().split('T')[0];

  form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    surname: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    patronymic: ['', Validators.maxLength(100)],
    // B-16: dateBirth is optional; B-13: no future dates
    dateBirth: ['', [pastOrPresentValidator]],
    // B-12: phone format validation
    phone: ['', [Validators.required, phoneValidator]],
    email: ['', [Validators.email, Validators.maxLength(200)]],
    // B-15: consent must be explicitly opted-in (not pre-checked)
    consentGiven: [false, [Validators.requiredTrue]],
    notificationsEnabled: [false]
  });

  search(): void {
    const q = this.searchQuery();
    if (!q.trim()) return;
    this.searched.set(true);
    this.clientService.search(q).subscribe({
      next: r => this.clients.set(r),
      error: () => { this.clients.set([]); this.toast.error('Ошибка поиска клиентов'); }
    });
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
    obs.subscribe({
      next: () => { this.toast.success(id ? 'Клиент обновлён' : 'Клиент создан'); this.showForm.set(false); this.search(); },
      error: (err) => this.toast.error(err?.error?.message ?? (id ? 'Не удалось обновить клиента' : 'Не удалось создать клиента'))
    });
  }

  giveConsent(id: number): void {
    this.clientService.giveConsent(id).subscribe({
      next: () => { this.toast.success('Согласие сохранено'); this.search(); },
      error: (err) => this.toast.error(err?.error?.message ?? 'Не удалось сохранить согласие')
    });
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
