import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../tokens/api-url.token';
import { CreateEmployeeRequest, Employee, UpdateEmployeeRequest } from '../models/employee.models';
import { RoleName } from '../models/auth.models';
import { Page } from '../models/page.models';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private http = inject(HttpClient);
  private base = `${inject(API_URL)}/admin/employees`;

  getAll(page = 0, size = 20): Observable<Page<Employee>> { return this.http.get<Page<Employee>>(this.base, { params: { page, size } }); }
  getById(id: number): Observable<Employee> { return this.http.get<Employee>(`${this.base}/${id}`); }
  getByRole(role: RoleName): Observable<Employee[]> { return this.http.get<Employee[]>(`${this.base}/role/${role}`); }
  create(req: CreateEmployeeRequest): Observable<Employee> { return this.http.post<Employee>(this.base, req); }
  update(id: number, req: UpdateEmployeeRequest): Observable<Employee> { return this.http.put<Employee>(`${this.base}/${id}`, req); }
  setPassword(id: number, password: string): Observable<void> { return this.http.patch<void>(`${this.base}/${id}/password`, { password }); }
  toggleBlock(id: number, blocked: boolean): Observable<void> { return this.http.patch<void>(`${this.base}/${id}/block`, { blocked }); }
  terminateSession(id: number): Observable<void> { return this.http.post<void>(`${this.base}/${id}/terminate-session`, {}); }
}
