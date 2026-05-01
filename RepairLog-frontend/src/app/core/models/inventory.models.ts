export interface InventoryItem {
  id: number;
  name: string;
  serialNumber?: string;
  degreeWearName?: string;
  device?: boolean;
  unitPrice: number;
  quantity: number;
  inStock: boolean;
  minStockLevel: number;
  stockStatus: string;
  createdAt?: string;
  preferredSupplierId?: number;
  preferredSupplierName?: string;
}

export interface CreateInventoryRequest {
  name: string;
  partNumber?: string;
  description?: string;
  quantity: number;
  minQuantity?: number;
  purchasePrice?: number;
  sellingPrice?: number;
}

export interface ReceiveInventoryRequest {
  quantity: number;
  comment?: string;
}
