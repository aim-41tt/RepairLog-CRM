import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../tokens/api-url.token';
import { Client, CreateClientRequest } from '../models/client.models';

@Injectable({ providedIn: 'root' })
export class ClientService {
  private http = inject(HttpClient);
  private base = `${inject(API_URL)}/receptionist/clients`;

  search(query: string): Observable<Client[]> { return this.http.get<Client[]>(`${this.base}/search`, { params: { query } }); }
  getById(id: number): Observable<Client> { return this.http.get<Client>(`${this.base}/${id}`); }
  create(req: CreateClientRequest): Observable<Client> { return this.http.post<Client>(this.base, req); }
  update(id: number, req: CreateClientRequest): Observable<Client> { return this.http.put<Client>(`${this.base}/${id}`, req); }
  giveConsent(id: number): Observable<void> { return this.http.post<void>(`${this.base}/${id}/consent`, {}); }
}
