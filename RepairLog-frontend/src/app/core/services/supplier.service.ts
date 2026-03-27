import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../tokens/api-url.token';
import { CreateSupplierRequest, Supplier } from '../models/supplier.models';
import { Page } from '../models/page.models';

@Injectable({ providedIn: 'root' })
export class SupplierService {
  private http = inject(HttpClient);
  private base = `${inject(API_URL)}/admin/suppliers`;

  getAll(page = 0, size = 20): Observable<Page<Supplier>> { return this.http.get<Page<Supplier>>(this.base, { params: { page, size } }); }
  getById(id: number): Observable<Supplier> { return this.http.get<Supplier>(`${this.base}/${id}`); }
  getActive(): Observable<Supplier[]> { return this.http.get<Supplier[]>(`${this.base}/active`); }
  create(req: CreateSupplierRequest): Observable<Supplier> { return this.http.post<Supplier>(this.base, req); }
  update(id: number, req: CreateSupplierRequest): Observable<Supplier> { return this.http.put<Supplier>(`${this.base}/${id}`, req); }
  toggleActive(id: number, active: boolean): Observable<void> { return this.http.patch<void>(`${this.base}/${id}/active`, { active }); }
  getByIntegrationType(type: string): Observable<Supplier[]> { return this.http.get<Supplier[]>(`${this.base}/integration-type/${type}`); }
}
