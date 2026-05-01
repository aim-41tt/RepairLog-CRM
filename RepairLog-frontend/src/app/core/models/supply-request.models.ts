export type SupplyRequestStatus =
  'NEW' | 'AUTO_FORMED' | 'APPROVED' | 'CANCELLED' |
  'ORDERED' | 'IN_TRANSIT' | 'DELIVERED' | 'PARTIALLY_DELIVERED';

export interface SupplyRequestItem {
  id: number;
  itemName: string;
  partNumber?: string;
  quantity: number;
  unitPrice?: number;
  totalPrice?: number;
  inventoryItemId?: number;
  inventoryItemName?: string;
}

export interface SupplyRequest {
  id: number;
  requestNumber: string;
  supplierName?: string;
  supplierId?: number;
  statusName: string;
  requestedByName: string;
  approvedByName?: string;
  totalAmount?: number;
  comment?: string;
  createdAt: string;
  expectedDeliveryDate?: string;
  items: SupplyRequestItem[];
  relatedRepairOrderId?: number;
  relatedOrderNumber?: string;
  source?: string;
  externalOrderId?: string;
  externalOrderStatus?: string;
}

export interface CreateSupplyRequestItemRequest {
  itemName: string;
  partNumber?: string;
  quantity: number;
  unitPrice?: number;
  inventoryItemId?: number;
}

export interface CreateSupplyRequestRequest {
  supplierId?: number;
  comment?: string;
  repairOrderId?: number;
  items: CreateSupplyRequestItemRequest[];
}

export interface AssignSupplierRequest {
  supplierId: number;
}

export interface CreateSupplierPaymentRequest {
  paidAmount: number;
  paymentMethod: string;
  transactionId?: string;
  comment?: string;
}

export interface CreateSupplierInvoiceRequest {
  invoiceNumber: string;
  invoiceDate: string;
  totalAmount: number;
  dueDate?: string;
}

export interface SupplierPaymentResponse {
  id: number;
  supplyRequestId: number;
  requestNumber: string;
  paidAmount: number;
  paymentMethod: string;
  paidAt: string;
  paidByName: string;
  transactionId?: string;
  comment?: string;
}

export interface SupplierInvoiceResponse {
  id: number;
  supplyRequestId: number;
  requestNumber: string;
  supplierId: number;
  supplierName: string;
  invoiceNumber: string;
  invoiceDate: string;
  totalAmount: number;
  dueDate?: string;
  status: string;
  createdAt: string;
}

export interface SupplyDashboardResponse {
  totalActiveRequests: number;
  pendingApprovalCount: number;
  autoFormedCount: number;
  orderedCount: number;
  inTransitCount: number;
  overdueCount: number;
  lowStockItemsCount: number;
  outOfStockItemsCount: number;
  totalPendingAmount: number;
}

export interface SupplySettingResponse {
  id: number;
  settingKey: string;
  settingValue: string;
  description?: string;
  lastModifiedAt?: string;
  modifiedByName?: string;
}

export interface SupplySetting {
  key: string;
  value: string;
  description?: string;
}
