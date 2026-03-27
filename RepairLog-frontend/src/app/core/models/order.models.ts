export interface Order {
  id: number;
  orderNumber: string;
  clientId: number;
  clientFullName: string;
  clientPhone: string;
  deviceId: number;
  deviceDescription: string;
  acceptedByName: string;
  assignedMasterName?: string;
  assignedMasterId?: number;
  currentStatusName: string;
  currentStatusId: number;
  priorityName: string;
  clientComplaint?: string;
  externalCondition?: string;
  warrantyRepair: boolean;
  estimatedCompletionDate?: string;
  actualCompletionDate?: string;
  totalAmount: number;
  paymentStatus: 'UNPAID' | 'PARTIALLY_PAID' | 'FULLY_PAID' | 'REFUNDED';
  createdAt: string;
}

export interface CreateOrderRequest {
  clientId: number;
  deviceId: number;
  priorityId?: number;
  clientComplaint?: string;
  externalCondition?: string;
  warrantyRepair?: boolean;
  estimatedCompletionDate?: string;
}

export interface UpdateOrderStatusRequest {
  statusId: number;
  comment?: string;
}

export interface StatusHistoryEntry {
  id: number;
  statusName: string;
  changedByName: string;
  changedAt: string;
  comment?: string;
}
