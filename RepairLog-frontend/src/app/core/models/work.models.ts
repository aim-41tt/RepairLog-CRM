export interface WorkItem {
  id: number;
  description: string;
  price: number;
  employeeName?: string;
  completedAt?: string;
}

export interface CreateWorkRequest {
  receiptId: number;
  description: string;
  price: number;
}
