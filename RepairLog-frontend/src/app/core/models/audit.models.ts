export interface AuditEntry {
  id: number;
  eventType?: string;
  employeeId: number;
  employeeName: string;
  ipAddress?: string;
  resourceType?: string;
  resourceId?: number;
  action?: string;
  result?: string;
  details: Record<string, any>;
  createdAt: string;
}
