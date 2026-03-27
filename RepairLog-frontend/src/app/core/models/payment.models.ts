export interface Payment {
  id: number;
  paidAmount: number;
  paymentMethod: string;
  paidAt: string;
  acceptedByName?: string;
}

export interface CreatePaymentRequest {
  receiptId: number;
  paidAmount: number;
  paymentMethod: string;
  transactionId?: string;
}
