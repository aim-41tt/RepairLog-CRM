import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { EmployeeService } from '../../../core/services/employee.service';
import { UserService } from '../../../core/services/user.service';
import { Employee, CreateEmployeeRequest, UpdateEmployeeRequest } from '../../../core/models/employee.models';
import { RoleName } from '../../../core/models/auth.models';
import { ConfirmService } from '../../../core/services/confirm.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-employees',
  standalone: true,
  imports: [PageLayoutComponent, ReactiveFormsModule, RouterLink, FormsModule],
  templateUrl: './employees.component.html',
  styleUrl: './employees.component.scss'
})
export class EmployeesComponent implements OnInit {
  private employeeService = inject(EmployeeService);
  private userService = inject(UserService);
  private fb = inject(FormBuilder);
  private confirmService = inject(ConfirmService);
  private toast = inject(ToastService);

  currentUserId = () => this.userService.profile()?.employeeId;

  employees = signal<Employee[]>([]);
  loading = signal(false);
  showForm = signal(false);
  editId = signal<number | null>(null);
  error = signal('');
  passwordChangeId = signal<number | null>(null);
  newPassword = signal('');
  page = signal(0);
  totalPages = signal(0);

  roles: RoleName[] = ['ADMIN', 'TECHNICIAN', 'RECEPTIONIST'];

  form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    surname: ['', [Validators.required, Validators.maxLength(100)]],
    patronymic: ['', Validators.maxLength(100)],
    dateBirth: ['', Validators.required],
    login: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
    password: ['', [Validators.minLength(8), Validators.maxLength(100)]],
    roles: [['TECHNICIAN'] as RoleName[]]
  });

  ngOnInit(): void { this.load(); }

  load(page = 0): void {
    this.loading.set(true);
    this.page.set(page);
    this.employeeService.getAll(page).subscribe({
      next: res => { this.employees.set(res.content); this.totalPages.set(res.totalPages); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Не удалось загрузить сотрудников'); }
    });
  }

  prev(): void { if (this.page() > 0) this.load(this.page() - 1); }
  next(): void { if (this.page() < this.totalPages() - 1) this.load(this.page() + 1); }

  /** Switch password field validators between create (required) and edit (optional) modes. */
  private setPasswordRequired(required: boolean): void {
    const passwordCtrl = this.form.get('password');
    if (!passwordCtrl) return;
    const baseValidators = [Validators.minLength(8), Validators.maxLength(100)];
    passwordCtrl.setValidators(required ? [Validators.required, ...baseValidators] : baseValidators);
    passwordCtrl.updateValueAndValidity({ emitEvent: false });
  }

  openCreate(): void {
    this.editId.set(null);
    this.setPasswordRequired(true);
    this.form.reset({ name: '', surname: '', patronymic: '', dateBirth: '', login: '', password: '', roles: ['TECHNICIAN'] });
    this.showForm.set(true);
  }

  openEdit(emp: Employee): void {
    this.editId.set(emp.id);
    this.setPasswordRequired(false);
    this.form.patchValue({ name: emp.name, surname: emp.surname, patronymic: emp.patronymic ?? '', dateBirth: emp.dateBirth ?? '', login: emp.login, password: '', roles: emp.roles });
    this.showForm.set(true);
  }

  submit(): void {
    if (this.form.invalid) return;
    const v = this.form.getRawValue();
    if (!this.editId() && !v.password) { this.toast.error('Пароль обязателен'); return; }
    if (this.editId()) {
      const req: UpdateEmployeeRequest = { name: v.name, surname: v.surname, patronymic: v.patronymic || undefined, dateBirth: v.dateBirth || undefined, roles: v.roles };
      this.employeeService.update(this.editId()!, req).subscribe({
        next: () => { this.toast.success('Сотрудник обновлён'); this.showForm.set(false); this.load(); },
        error: (err) => this.toast.error(err?.error?.message ?? 'Не удалось обновить сотрудника')
      });
    } else {
      const req: CreateEmployeeRequest = { name: v.name, surname: v.surname, patronymic: v.patronymic || undefined, dateBirth: v.dateBirth, login: v.login, password: v.password, roles: v.roles };
      this.employeeService.create(req).subscribe({
        next: () => { this.toast.success('Сотрудник создан'); this.showForm.set(false); this.load(); },
        error: (err) => this.toast.error(err?.error?.message ?? 'Не удалось создать сотрудника')
      });
    }
  }

  hasFormRole(role: RoleName): boolean {
    return (this.form.getRawValue().roles ?? []).includes(role);
  }

  toggleRole(role: RoleName): void {
    const current = this.form.getRawValue().roles ?? [];
    const updated = current.includes(role)
      ? current.filter(r => r !== role)
      : [...current, role];
    this.form.patchValue({ roles: updated });
  }

  roleLabel(role: RoleName): string {
    const map: Record<RoleName, string> = { ADMIN: 'Администратор', TECHNICIAN: 'Мастер', RECEPTIONIST: 'Приёмщик' };
    return map[role] ?? role;
  }

  toggleBlock(emp: Employee): void {
    const action = emp.blocked ? 'Разблокировать' : 'Заблокировать';
    this.confirmService.confirm({
      title: `${action} сотрудника`,
      message: `${action} «${emp.surname} ${emp.name}»?`,
      confirmLabel: action,
      variant: emp.blocked ? 'warning' : 'danger'
    }).subscribe(ok => {
      if (!ok) return;
      this.employeeService.toggleBlock(emp.id, !emp.blocked).subscribe({
        next: () => { this.toast.success(emp.blocked ? 'Сотрудник разблокирован' : 'Сотрудник заблокирован'); this.load(); },
        error: (err) => this.toast.error(err?.error?.message ?? 'Не удалось изменить статус')
      });
    });
  }

  openPasswordChange(empId: number): void {
    this.passwordChangeId.set(empId);
    this.newPassword.set('');
  }

  changePassword(): void {
    const id = this.passwordChangeId();
    const pwd = this.newPassword();
    if (!id || !pwd) return;
    this.employeeService.setPassword(id, pwd).subscribe({
      next: () => {
        this.toast.success('Пароль изменён');
        this.passwordChangeId.set(null);
        this.newPassword.set('');
      },
      error: (err) => this.toast.error(err?.error?.message ?? 'Не удалось изменить пароль')
    });
  }

  terminateSession(emp: Employee): void {
    this.confirmService.confirm({
      title: 'Завершить сессию',
      message: `Завершить сессию «${emp.surname} ${emp.name}»? Сотрудник будет разлогинен.`,
      confirmLabel: 'Завершить',
      variant: 'danger'
    }).subscribe(ok => {
      if (!ok) return;
      this.employeeService.terminateSession(emp.id).subscribe({
        next: () => this.toast.success('Сессия завершена'),
        error: (err) => this.toast.error(err?.error?.message ?? 'Не удалось завершить сессию')
      });
    });
  }
}
