import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../tokens/api-url.token';
import { CreateInventoryRequest, InventoryItem, ReceiveInventoryRequest } from '../models/inventory.models';
import { Page } from '../models/page.models';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);

  // Technician
  getAll(): Observable<InventoryItem[]> { return this.http.get<InventoryItem[]>(`${this.apiUrl}/technician/inventory`); }
  search(query: string): Observable<InventoryItem[]> { return this.http.get<InventoryItem[]>(`${this.apiUrl}/technician/inventory/search`, { params: { query } }); }
  getLowStock(): Observable<InventoryItem[]> { return this.http.get<InventoryItem[]>(`${this.apiUrl}/technician/inventory/low-stock`); }
  consume(itemId: number, quantity: number, orderId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/technician/inventory/${itemId}/consume`, null, { params: { quantity: quantity.toString(), orderId: orderId.toString() } });
  }

  // Admin
  adminGetAll(page = 0, size = 20): Observable<Page<InventoryItem>> { return this.http.get<Page<InventoryItem>>(`${this.apiUrl}/admin/inventory`, { params: { page, size } }); }
  adminGetLowStock(): Observable<InventoryItem[]> { return this.http.get<InventoryItem[]>(`${this.apiUrl}/admin/inventory/low-stock`); }
  adminCreate(req: CreateInventoryRequest): Observable<InventoryItem> { return this.http.post<InventoryItem>(`${this.apiUrl}/admin/inventory`, req); }
  adminReceive(itemId: number, req: ReceiveInventoryRequest): Observable<void> { return this.http.post<void>(`${this.apiUrl}/admin/inventory/${itemId}/receive`, req); }
  adminDelete(itemId: number): Observable<void> { return this.http.delete<void>(`${this.apiUrl}/admin/inventory/${itemId}`); }
}
