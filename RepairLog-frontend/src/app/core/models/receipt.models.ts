import { WorkItem } from './work.models';
import { Payment } from './payment.models';

export interface Receipt {
  id: number;
  repairOrderId: number;
  orderNumber: string;
  subtotal: number;
  discountAmount: number;
  taxAmount: number;
  totalAmount: number;
  paymentStatus: string;
  locked: boolean;
  createdAt: string;
  works: WorkItem[];
  payments: Payment[];
}
