export interface RefField {
  id?: number;
  name?: string;
}

export interface Device {
  id: number;
  deviceTypeName: string;
  brandName: string;
  modelName: string;
  serialNumber?: string;
  clientOwned: boolean;
  clientId?: number;
  clientFullName?: string;
  description?: string;
  createdAt: string;
}

export interface CreateDeviceRequest {
  deviceType: RefField;
  brand: RefField;
  model: RefField;
  clientId?: number;
  serialNumber?: string;
  clientOwned?: boolean;
}
