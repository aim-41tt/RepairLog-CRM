import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../tokens/api-url.token';
import { CreateWorkRequest, WorkItem } from '../models/work.models';

@Injectable({ providedIn: 'root' })
export class WorkService {
  private http = inject(HttpClient);
  private base = `${inject(API_URL)}/technician`;

  create(req: CreateWorkRequest): Observable<WorkItem> {
    return this.http.post<WorkItem>(`${this.base}/works`, req);
  }
}
