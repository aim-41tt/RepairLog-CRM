import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../tokens/api-url.token';
import { AuditEntry } from '../models/audit.models';
import { Page } from '../models/page.models';

@Injectable({ providedIn: 'root' })
export class AuditService {
  private http = inject(HttpClient);
  private base = `${inject(API_URL)}/admin/audit`;

  getAll(page = 0, size = 20): Observable<Page<AuditEntry>> { return this.http.get<Page<AuditEntry>>(this.base, { params: { page, size } }); }
  getByEmployee(employeeId: number): Observable<AuditEntry[]> { return this.http.get<AuditEntry[]>(`${this.base}/employee/${employeeId}`); }
  getByPeriod(from: string, to: string): Observable<Page<AuditEntry>> { return this.http.get<Page<AuditEntry>>(`${this.base}/period`, { params: { from, to } }); }
}
