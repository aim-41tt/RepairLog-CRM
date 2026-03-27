export interface Diagnostic {
  id: number;
  repairOrderId: number;
  orderNumber: string;
  description: string;
  solution?: string;
  performedByName: string;
  createdAt: string;
}

export interface CreateDiagnosticRequest {
  repairOrderId: number;
  description: string;
  solution?: string;
}
