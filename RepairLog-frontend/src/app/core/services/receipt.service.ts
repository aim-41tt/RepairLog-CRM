import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../tokens/api-url.token';
import { Receipt } from '../models/receipt.models';

@Injectable({ providedIn: 'root' })
export class ReceiptService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);

  getByOrder(orderId: number, role: 'receptionist' | 'technician'): Observable<Receipt> {
    return this.http.get<Receipt>(`${this.apiUrl}/${role}/receipts/order/${orderId}`);
  }
}
