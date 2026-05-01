import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { ClientService } from '../../../core/services/client.service';
import { DeviceService } from '../../../core/services/device.service';
import { OrderService } from '../../../core/services/order.service';
import { ReferenceService } from '../../../core/services/reference.service';
import { ToastService } from '../../../core/services/toast.service';
import { DocumentService } from '../../../core/services/document.service';
import { Client } from '../../../core/models/client.models';
import { Device } from '../../../core/models/device.models';
import { Order } from '../../../core/models/order.models';
import { Priority, DeviceType, Brand, DeviceModel } from '../../../core/models/reference.models';

type Step = 'client' | 'device' | 'order' | 'done';

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
  selector: 'app-create-order',
  standalone: true,
  imports: [PageLayoutComponent, ReactiveFormsModule, RouterLink],
  templateUrl: './create-order.component.html',
  styleUrl: './create-order.component.scss'
})
export class CreateOrderComponent implements OnInit {
  private clientService = inject(ClientService);
  private deviceService = inject(DeviceService);
  private orderService = inject(OrderService);
  private referenceService = inject(ReferenceService);
  private toast = inject(ToastService);
  private documentService = inject(DocumentService);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  step = signal<Step>('client');
  selectedClient = signal<Client | null>(null);
  selectedDevice = signal<Device | null>(null);
  clientDevices = signal<Device[]>([]);
  searchResults = signal<Client[]>([]);
  searchQuery = signal('');
  searchDone = signal(false);
  showClientForm = signal(false);
  createdOrder = signal<Order | null>(null);
  /** Guards against double-submission (network can take >1s) */
  submitting = signal(false);

  priorities = signal<Priority[]>([]);
  deviceTypes = signal<DeviceType[]>([]);
  brands = signal<Brand[]>([]);
  models = signal<DeviceModel[]>([]);

  // DeviceType search-or-create
  deviceTypeSearch = signal('');
  filteredDeviceTypes = signal<DeviceType[]>([]);
  selectedDeviceType = signal<DeviceType | null>(null);

  // Brand/model search-or-create
  brandSearch = signal('');
  modelSearch = signal('');
  filteredBrands = signal<Brand[]>([]);
  filteredModels = signal<DeviceModel[]>([]);
  selectedBrand = signal<Brand | null>(null);
  selectedModel = signal<DeviceModel | null>(null);

  /** ISO date string for today — used as max on date input (B-13) */
  readonly todayIso = new Date().toISOString().split('T')[0];

  clientForm = this.fb.nonNullable.group({
    surname: ['', Validators.required],
    name: ['', Validators.required],
    patronymic: [''],
    // B-12: pattern validator for phone format
    phone: ['', [Validators.required, phoneValidator]],
    // B-13: date not in future; B-16: no longer required (optional field)
    dateBirth: ['', [pastOrPresentValidator]],
    email: ['', [Validators.email]],
    // B-15: must be explicitly checked by user (not pre-checked)
    consentGiven: [false, [Validators.requiredTrue]],
    notificationsEnabled: [false]
  });

  deviceForm = this.fb.nonNullable.group({
    serialNumber: ['']
  });

  orderForm = this.fb.nonNullable.group({
    priorityId: [0, [Validators.required, Validators.min(1)]],
    clientComplaint: ['', Validators.required],
    externalCondition: [''],
    estimatedCompletionDate: [''],
    warrantyRepair: [false]
  });

  ngOnInit(): void {
    this.referenceService.getPriorities().subscribe({ next: p => this.priorities.set(p) });
    this.referenceService.getDeviceTypes().subscribe({ next: t => this.deviceTypes.set(t) });
    this.referenceService.getBrands().subscribe({ next: b => this.brands.set(b) });
  }

  // ── Step 1: Client ──

  searchClients(): void {
    const q = this.searchQuery();
    if (!q.trim()) return;
    this.searchDone.set(true);
    this.showClientForm.set(false);
    this.clientService.search(q).subscribe({
      next: r => this.searchResults.set(r),
      error: () => { this.searchResults.set([]); this.toast.error('Ошибка поиска клиентов'); }
    });
  }

  selectClient(c: Client): void {
    this.selectedClient.set(c);
    this.deviceService.getByClient(c.id).subscribe({
      next: d => this.clientDevices.set(d),
      error: () => { this.clientDevices.set([]); this.toast.error('Не удалось загрузить устройства клиента'); }
    });
    this.step.set('device');
  }

  createClient(): void {
    if (this.clientForm.invalid) return;
    const v = this.clientForm.getRawValue();
    this.clientService.create(v).subscribe({
      next: c => {
        this.toast.success('Клиент создан');
        this.selectClient(c);
      },
      error: (err) => this.toast.error(err?.error?.message ?? 'Не удалось создать клиента')
    });
  }

  // ── Step 2: Device (deviceType/brand/model search-or-create) ──

  selectDevice(d: Device): void {
    this.selectedDevice.set(d);
    this.step.set('order');
  }

  filterDeviceTypes(): void {
    const q = this.deviceTypeSearch().toLowerCase().trim();
    if (!q) { this.filteredDeviceTypes.set(this.deviceTypes()); return; }
    this.filteredDeviceTypes.set(this.deviceTypes().filter(t => t.name.toLowerCase().includes(q)));
  }

  pickDeviceType(t: DeviceType): void {
    this.selectedDeviceType.set(t);
    this.deviceTypeSearch.set(t.name);
    this.filteredDeviceTypes.set([]);
  }

  createNewDeviceType(): void {
    const name = this.deviceTypeSearch().trim();
    if (!name) return;
    this.referenceService.createDeviceType(name).subscribe({
      next: t => {
        this.deviceTypes.update(arr => [...arr, t]);
        this.pickDeviceType(t);
        this.toast.success(`Тип «${t.name}» создан`);
      },
      error: (err) => this.toast.error(err?.error?.message ?? 'Не удалось создать тип')
    });
  }

  filterBrands(): void {
    const q = this.brandSearch().toLowerCase().trim();
    if (!q) { this.filteredBrands.set(this.brands()); return; }
    this.filteredBrands.set(this.brands().filter(b => b.name.toLowerCase().includes(q)));
  }

  pickBrand(b: Brand): void {
    this.selectedBrand.set(b);
    this.brandSearch.set(b.name);
    this.filteredBrands.set([]);
    this.selectedModel.set(null);
    this.modelSearch.set('');
    this.filteredModels.set([]);
    this.referenceService.getBrandModels(b.id).subscribe({ next: m => this.models.set(m) });
  }

  createNewBrand(): void {
    const name = this.brandSearch().trim();
    if (!name) return;
    this.referenceService.createBrand(name).subscribe({
      next: b => {
        this.brands.update(arr => [...arr, b]);
        this.pickBrand(b);
        this.toast.success(`Марка «${b.name}» создана`);
      },
      error: (err) => this.toast.error(err?.error?.message ?? 'Не удалось создать марку')
    });
  }

  filterModels(): void {
    const q = this.modelSearch().toLowerCase().trim();
    if (!q) { this.filteredModels.set(this.models()); return; }
    this.filteredModels.set(this.models().filter(m => m.name.toLowerCase().includes(q)));
  }

  pickModel(m: DeviceModel): void {
    this.selectedModel.set(m);
    this.modelSearch.set(m.name);
    this.filteredModels.set([]);
  }

  createNewModel(): void {
    const brand = this.selectedBrand();
    const name = this.modelSearch().trim();
    if (!brand || !name) return;
    this.referenceService.createModel(brand.id, name).subscribe({
      next: m => {
        this.models.update(arr => [...arr, m]);
        this.pickModel(m);
        this.toast.success(`Модель «${m.name}» создана`);
      },
      error: (err) => this.toast.error(err?.error?.message ?? 'Не удалось создать модель')
    });
  }

  createNewDevice(): void {
    const v = this.deviceForm.getRawValue();
    const devType = this.selectedDeviceType();
    const brand = this.selectedBrand();
    const model = this.selectedModel();
    if (!devType) {
      this.toast.warning('Выберите или создайте тип устройства');
      return;
    }
    if (!brand || !model) {
      this.toast.warning('Выберите или создайте марку и модель');
      return;
    }
    const clientId = this.selectedClient()!.id;
    this.deviceService.create({
      deviceType: { id: devType.id },
      brand: { id: brand.id },
      model: { id: model.id },
      clientId,
      serialNumber: v.serialNumber || undefined,
      clientOwned: true
    }).subscribe({
      next: d => { this.toast.success('Устройство создано'); this.selectedDevice.set(d); this.step.set('order'); },
      error: (err) => this.toast.error(err?.error?.message ?? 'Не удалось создать устройство')
    });
  }

  // ── Step 3: Order ──

  submitOrder(): void {
    if (this.orderForm.invalid || this.submitting()) return;
    this.submitting.set(true);
    const v = this.orderForm.getRawValue();
    this.orderService.createOrder({
      clientId: this.selectedClient()!.id,
      deviceId: this.selectedDevice()!.id,
      priorityId: v.priorityId,
      clientComplaint: v.clientComplaint,
      externalCondition: v.externalCondition || undefined,
      estimatedCompletionDate: v.estimatedCompletionDate || undefined,
      warrantyRepair: v.warrantyRepair
    }).subscribe({
      next: order => {
        this.submitting.set(false);
        this.createdOrder.set(order);
        this.step.set('done');
        this.toast.success('Заявка создана');
      },
      error: (err) => {
        this.submitting.set(false);
        this.toast.error(err?.error?.message ?? 'Не удалось создать заявку');
      }
    });
  }

  // ── Phone formatting ──

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
    this.clientForm.get('phone')?.setValue(formatted, { emitEvent: false });
  }

  // ── Step 4: Done ──

  printReceipt(): void {
    const order = this.createdOrder();
    if (order) {
      this.documentService.generateReceipt(order.id, 'receptionist');
    }
  }

  createAnother(): void {
    this.step.set('client');
    this.selectedClient.set(null);
    this.selectedDevice.set(null);
    this.createdOrder.set(null);
    this.searchResults.set([]);
    this.searchQuery.set('');
    this.searchDone.set(false);
    this.showClientForm.set(false);
    this.clientDevices.set([]);
    this.selectedDeviceType.set(null);
    this.deviceTypeSearch.set('');
    this.filteredDeviceTypes.set([]);
    this.selectedBrand.set(null);
    this.selectedModel.set(null);
    this.brandSearch.set('');
    this.modelSearch.set('');
  }
}
