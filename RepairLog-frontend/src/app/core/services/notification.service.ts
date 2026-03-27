import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../tokens/api-url.token';
import { Notification } from '../models/notification.models';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private http = inject(HttpClient);
  private base = `${inject(API_URL)}/admin/notifications`;

  getPending(): Observable<Notification[]> { return this.http.get<Notification[]>(`${this.base}/pending`); }
  markSent(id: number): Observable<void> { return this.http.post<void>(`${this.base}/${id}/sent`, {}); }
}
