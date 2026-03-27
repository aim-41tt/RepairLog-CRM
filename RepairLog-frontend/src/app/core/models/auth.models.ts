export type RoleName = 'ADMIN' | 'TECHNICIAN' | 'RECEPTIONIST';

export interface LoginRequest {
  login: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  employeeId: number;
  fullName: string;
  roles: RoleName[];
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}
