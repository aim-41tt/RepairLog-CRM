export interface Client {
  id: number;
  name: string;
  surname: string;
  patronymic?: string;
  fullName?: string;
  dateBirth?: string;
  phone: string;
  email?: string;
  consentGiven: boolean;
  consentDate?: string;
  dataRetentionUntil?: string;
  notificationsEnabled: boolean;
  createdAt: string;
}

export interface CreateClientRequest {
  name: string;
  surname: string;
  patronymic?: string;
  dateBirth: string;
  phone: string;
  email?: string;
  consentGiven: boolean;
  notificationsEnabled: boolean;
}
