export interface Notification {
  id: number;
  type: string;
  clientId?: number;
  clientName?: string;
  repairOrderId?: number;
  orderNumber?: string;
  channel?: string;
  status: string;
  subject?: string;
  messageBody?: string;
  sentAt?: string;
  createdAt: string;
}
