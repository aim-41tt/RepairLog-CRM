import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_URL } from '../tokens/api-url.token';
import { CreateOrderRequest, Order, StatusHistoryEntry, UpdateOrderStatusRequest } from '../models/order.models';
import { Diagnostic } from '../models/diagnostic.models';
import { Receipt } from '../models/receipt.models';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);

  // Receptionist
  createOrder(req: CreateOrderRequest): Observable<Order> {
    return this.http.post<Order>(`${this.apiUrl}/receptionist/orders`, req);
  }
  getOrderByIdReceptionist(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.apiUrl}/receptionist/orders/${id}`);
  }
  searchByNumber(orderNumber: string): Observable<Order[]> {
    return this.http.get<Order>(`${this.apiUrl}/receptionist/orders/search`, { params: { orderNumber } })
      .pipe(map(o => [o]));
  }
  searchMulti(query: string): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/receptionist/orders/search/multi`, { params: { query } });
  }
  updateStatusReceptionist(id: number, req: UpdateOrderStatusRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/receptionist/orders/${id}/status`, req);
  }
  getOrderDiagnosticsReceptionist(orderId: number): Observable<Diagnostic> {
    return this.http.get<Diagnostic>(`${this.apiUrl}/receptionist/orders/${orderId}/diagnostics`);
  }
  getReceiptReceptionist(orderId: number): Observable<Receipt> {
    return this.http.get<Receipt>(`${this.apiUrl}/receptionist/receipts/order/${orderId}`);
  }

  // Technician
  getUnassigned(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/technician/orders/unassigned`);
  }
  getMyOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/technician/orders/my`);
  }
  getOrderByIdTechnician(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.apiUrl}/technician/orders/${id}`);
  }
  takeOrder(id: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/technician/orders/${id}/take`, {});
  }
  updateStatusTechnician(id: number, req: UpdateOrderStatusRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/technician/orders/${id}/status`, req);
  }

  getStatusHistory(orderId: number, role: 'technician' | 'receptionist'): Observable<StatusHistoryEntry[]> {
    return this.http.get<StatusHistoryEntry[]>(`${this.apiUrl}/${role}/orders/${orderId}/status-history`);
  }
}
