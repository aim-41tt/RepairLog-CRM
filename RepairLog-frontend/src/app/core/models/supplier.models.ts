export interface Supplier {
  id: number;
  name: string;
  contactPerson?: string;
  phone?: string;
  email?: string;
  address?: string;
  inn?: string;
  active: boolean;
  createdAt: string;
  integrationType?: string;
  priceSource?: string;
  orderMethod?: string;
  websiteUrl?: string;
  contactMessenger?: string;
  priceListEmail?: string;
  externalSupplierId?: string;
}

export interface CreateSupplierRequest {
  name: string;
  contactPerson?: string;
  phone?: string;
  email?: string;
  address?: string;
  inn?: string;
  integrationType?: string;
}
