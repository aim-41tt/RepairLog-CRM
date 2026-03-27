import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, map, catchError } from 'rxjs';
import { API_URL } from '../tokens/api-url.token';
import { CreateDiagnosticRequest, Diagnostic } from '../models/diagnostic.models';

@Injectable({ providedIn: 'root' })
export class DiagnosticService {
  private http = inject(HttpClient);
  private base = `${inject(API_URL)}/technician`;

  getByOrder(orderId: number): Observable<Diagnostic[]> {
    return this.http.get<Diagnostic>(`${this.base}/diagnostics/order/${orderId}`).pipe(
      map(d => [d]),
      catchError(() => of([]))
    );
  }
  create(req: CreateDiagnosticRequest): Observable<Diagnostic> {
    return this.http.post<Diagnostic>(`${this.base}/diagnostics`, req);
  }

  update(id: number, req: { description: string; solution?: string }): Observable<Diagnostic> {
    return this.http.put<Diagnostic>(`${this.base}/diagnostics/${id}`, req);
  }
}
