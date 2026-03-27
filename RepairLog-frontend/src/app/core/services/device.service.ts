import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../tokens/api-url.token';
import { Device, CreateDeviceRequest } from '../models/device.models';

@Injectable({ providedIn: 'root' })
export class DeviceService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);

  getByClient(clientId: number): Observable<Device[]> {
    return this.http.get<Device[]>(`${this.apiUrl}/receptionist/devices/client/${clientId}`);
  }
  create(req: CreateDeviceRequest): Observable<Device> {
    return this.http.post<Device>(`${this.apiUrl}/receptionist/devices`, req);
  }
  moveDevice(deviceId: number, location: string, comment?: string): Observable<void> {
    const params: Record<string, string> = { location };
    if (comment) params['comment'] = comment;
    return this.http.post<void>(`${this.apiUrl}/technician/devices/${deviceId}/move`, null, { params });
  }
}
