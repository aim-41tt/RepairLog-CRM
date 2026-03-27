import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../tokens/api-url.token';
import { Brand, DeviceModel, DeviceType, DegreeWear, Priority, RepairStatus } from '../models/reference.models';

@Injectable({ providedIn: 'root' })
export class ReferenceService {
  private http = inject(HttpClient);
  private base = `${inject(API_URL)}/reference`;

  getDeviceTypes(): Observable<DeviceType[]> { return this.http.get<DeviceType[]>(`${this.base}/device-types`); }
  getBrands(): Observable<Brand[]> { return this.http.get<Brand[]>(`${this.base}/brands`); }
  createBrand(name: string): Observable<Brand> { return this.http.post<Brand>(`${this.base}/brands`, { name }); }
  getBrandModels(brandId: number): Observable<DeviceModel[]> { return this.http.get<DeviceModel[]>(`${this.base}/brands/${brandId}/models`); }
  createModel(brandId: number, name: string): Observable<DeviceModel> { return this.http.post<DeviceModel>(`${this.base}/brands/${brandId}/models`, { name }); }
  getRepairStatuses(): Observable<RepairStatus[]> { return this.http.get<RepairStatus[]>(`${this.base}/repair-statuses`); }
  getPriorities(): Observable<Priority[]> { return this.http.get<Priority[]>(`${this.base}/priorities`); }
  getDegreeWears(): Observable<DegreeWear[]> { return this.http.get<DegreeWear[]>(`${this.base}/degree-wears`); }
}
