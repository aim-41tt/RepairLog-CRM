export interface NamedRef {
  id: number;
  name: string;
}

export type DeviceType = NamedRef;
export type Brand = NamedRef;
export type DeviceModel = NamedRef;
export type RepairStatus = NamedRef;
export type DegreeWear = NamedRef;

export interface Priority {
  id: number;
  name: string;
  sortOrder: number;
  colorHex: string;
}
