import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { ClientService } from '../../../core/services/client.service';
import { DeviceService } from '../../../core/services/device.service';
import { OrderService } from '../../../core/services/order.service';
import { ReferenceService } from '../../../core/services/reference.service';
import { ToastService } from '../../../core/services/toast.service';
import { Client } from '../../../core/models/client.models';
import { Device } from '../../../core/models/device.models';
import { Order } from '../../../core/models/order.models';
import { Priority, DeviceType, Brand, DeviceModel } from '../../../core/models/reference.models';

type Step = 'client' | 'device' | 'order' | 'done';

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

  priorities = signal<Priority[]>([]);
  deviceTypes = signal<DeviceType[]>([]);
  brands = signal<Brand[]>([]);
  models = signal<DeviceModel[]>([]);

  // Brand/model search-or-create
  brandSearch = signal('');
  modelSearch = signal('');
  filteredBrands = signal<Brand[]>([]);
  filteredModels = signal<DeviceModel[]>([]);
  selectedBrand = signal<Brand | null>(null);
  selectedModel = signal<DeviceModel | null>(null);

  clientForm = this.fb.nonNullable.group({
    surname: ['', Validators.required],
    name: ['', Validators.required],
    patronymic: [''],
    phone: ['', Validators.required],
    dateBirth: ['', Validators.required],
    email: [''],
    consentGiven: [true],
    notificationsEnabled: [true]
  });

  deviceForm = this.fb.nonNullable.group({
    deviceTypeId: [0, Validators.required],
    serialNumber: ['']
  });

  orderForm = this.fb.nonNullable.group({
    priorityId: [0, Validators.required],
    clientComplaint: ['', Validators.required]
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
    this.clientService.search(q).subscribe({ next: r => this.searchResults.set(r) });
  }

  selectClient(c: Client): void {
    this.selectedClient.set(c);
    this.deviceService.getByClient(c.id).subscribe({ next: d => this.clientDevices.set(d) });
    this.step.set('device');
  }

  createClient(): void {
    if (this.clientForm.invalid) return;
    const v = this.clientForm.getRawValue();
    this.clientService.create(v).subscribe({
      next: c => {
        this.toast.success('Клиент создан');
        this.selectClient(c);
      }
    });
  }

  // ── Step 2: Device (brand/model search-or-create) ──

  selectDevice(d: Device): void {
    this.selectedDevice.set(d);
    this.step.set('order');
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
      }
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
      }
    });
  }

  createNewDevice(): void {
    const v = this.deviceForm.getRawValue();
    const brand = this.selectedBrand();
    const model = this.selectedModel();
    if (!brand || !model) {
      this.toast.warning('Выберите или создайте марку и модель');
      return;
    }
    const clientId = this.selectedClient()!.id;
    this.deviceService.create({
      deviceType: { id: v.deviceTypeId },
      brand: { id: brand.id },
      model: { id: model.id },
      clientId,
      serialNumber: v.serialNumber || undefined,
      clientOwned: true
    }).subscribe({
      next: d => { this.selectedDevice.set(d); this.step.set('order'); }
    });
  }

  // ── Step 3: Order ──

  submitOrder(): void {
    if (this.orderForm.invalid) return;
    const v = this.orderForm.getRawValue();
    this.orderService.createOrder({
      clientId: this.selectedClient()!.id,
      deviceId: this.selectedDevice()!.id,
      priorityId: v.priorityId,
      clientComplaint: v.clientComplaint
    }).subscribe({
      next: order => {
        this.createdOrder.set(order);
        this.step.set('done');
        this.toast.success('Заявка создана');
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
    window.print();
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
    this.selectedBrand.set(null);
    this.selectedModel.set(null);
    this.brandSearch.set('');
    this.modelSearch.set('');
  }
}
