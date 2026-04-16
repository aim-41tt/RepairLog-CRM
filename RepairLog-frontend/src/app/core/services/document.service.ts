import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from '../tokens/api-url.token';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);

  generateReceipt(orderId: number, role: 'receptionist' | 'technician'): void {
    this.openPdf(`${this.apiUrl}/${role}/orders/${orderId}/documents/receipt`);
  }

  generateCompletionAct(orderId: number, role: 'receptionist' | 'technician'): void {
    this.openPdf(`${this.apiUrl}/${role}/orders/${orderId}/documents/completion-act`);
  }

  generateWarrantyCard(orderId: number, role: 'receptionist' | 'technician'): void {
    this.openPdf(`${this.apiUrl}/${role}/orders/${orderId}/documents/warranty-card`);
  }

  generateRejectionSheet(orderId: number, reason: string, role: 'receptionist' | 'technician'): void {
    this.openPdf(`${this.apiUrl}/${role}/orders/${orderId}/documents/rejection-sheet?reason=${encodeURIComponent(reason)}`);
  }

  private openPdf(url: string): void {
    this.http.get(url, { responseType: 'blob' }).subscribe({
      next: (blob) => {
        const fileUrl = URL.createObjectURL(blob);
        window.open(fileUrl, '_blank');
      },
      error: (err) => {
        console.error('Ошибка генерации документа:', err);
      }
    });
  }
}
