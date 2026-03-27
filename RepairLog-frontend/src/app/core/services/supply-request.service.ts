import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../tokens/api-url.token';
import {
  AssignSupplierRequest, CreateSupplierInvoiceRequest, CreateSupplierPaymentRequest,
  CreateSupplyRequestItemRequest, CreateSupplyRequestRequest, SupplyRequest, SupplyRequestStatus,
  SupplierPaymentResponse, SupplierInvoiceResponse, SupplyDashboardResponse, SupplySettingResponse
} from '../models/supply-request.models';
import { Page } from '../models/page.models';

@Injectable({ providedIn: 'root' })
export class SupplyRequestService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);
  private adminBase = `${this.apiUrl}/admin/supply-requests`;
  private techBase = `${this.apiUrl}/technician/supply-requests`;

  // Technician
  myRequests(): Observable<SupplyRequest[]> { return this.http.get<SupplyRequest[]>(`${this.techBase}/my`); }
  techCreate(req: CreateSupplyRequestRequest): Observable<SupplyRequest> { return this.http.post<SupplyRequest>(this.techBase, req); }

  // Admin
  getAll(page = 0, size = 20): Observable<Page<SupplyRequest>> { return this.http.get<Page<SupplyRequest>>(this.adminBase, { params: { page, size } }); }
  getById(id: number): Observable<SupplyRequest> { return this.http.get<SupplyRequest>(`${this.adminBase}/${id}`); }
  getByStatus(status: SupplyRequestStatus): Observable<SupplyRequest[]> { return this.http.get<SupplyRequest[]>(`${this.adminBase}/status/${status}`); }
  adminCreate(req: CreateSupplyRequestRequest): Observable<SupplyRequest> { return this.http.post<SupplyRequest>(this.adminBase, req); }
  approve(id: number): Observable<void> { return this.http.post<void>(`${this.adminBase}/${id}/approve`, {}); }
  cancel(id: number): Observable<void> { return this.http.post<void>(`${this.adminBase}/${id}/cancel`, {}); }
  markOrdered(id: number): Observable<void> { return this.http.post<void>(`${this.adminBase}/${id}/ordered`, {}); }
  markInTransit(id: number): Observable<void> { return this.http.post<void>(`${this.adminBase}/${id}/in-transit`, {}); }
  markDelivered(id: number): Observable<void> { return this.http.post<void>(`${this.adminBase}/${id}/delivered`, {}); }
  assignSupplier(id: number, req: AssignSupplierRequest): Observable<void> { return this.http.post<void>(`${this.adminBase}/${id}/assign-supplier`, req); }
  addItem(id: number, item: CreateSupplyRequestItemRequest): Observable<SupplyRequest> { return this.http.post<SupplyRequest>(`${this.adminBase}/${id}/items`, item); }
  updateItem(id: number, itemId: number, item: CreateSupplyRequestItemRequest): Observable<SupplyRequest> { return this.http.put<SupplyRequest>(`${this.adminBase}/${id}/items/${itemId}`, item); }
  deleteItem(id: number, itemId: number): Observable<SupplyRequest> { return this.http.delete<SupplyRequest>(`${this.adminBase}/${id}/items/${itemId}`); }
  updateComment(id: number, comment: string): Observable<SupplyRequest> { return this.http.put<SupplyRequest>(`${this.adminBase}/${id}/comment`, { comment }); }
  addPayment(id: number, req: CreateSupplierPaymentRequest): Observable<void> { return this.http.post<void>(`${this.adminBase}/${id}/payment`, req); }
  getPayments(id: number): Observable<SupplierPaymentResponse[]> { return this.http.get<SupplierPaymentResponse[]>(`${this.adminBase}/${id}/payments`); }
  addInvoice(id: number, req: CreateSupplierInvoiceRequest): Observable<void> { return this.http.post<void>(`${this.adminBase}/${id}/invoice`, req); }
  getInvoices(id: number): Observable<SupplierInvoiceResponse[]> { return this.http.get<SupplierInvoiceResponse[]>(`${this.adminBase}/${id}/invoices`); }
  getDashboard(): Observable<SupplyDashboardResponse> { return this.http.get<SupplyDashboardResponse>(`${this.apiUrl}/admin/supply-dashboard`); }
  getSettings(): Observable<SupplySettingResponse[]> { return this.http.get<SupplySettingResponse[]>(`${this.apiUrl}/admin/supply-settings`); }
  updateSetting(key: string, value: string): Observable<void> { return this.http.put<void>(`${this.apiUrl}/admin/supply-settings/${key}`, { settingValue: value }); }
}
