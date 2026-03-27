import { RoleName } from './auth.models';

export interface Employee {
  id: number;
  name: string;
  surname: string;
  patronymic?: string;
  fullName: string;
  dateBirth?: string;
  login: string;
  blocked: boolean;
  lastLogin?: string;
  createdAt: string;
  roles: RoleName[];
}

export interface CreateEmployeeRequest {
  name: string;
  surname: string;
  patronymic?: string;
  dateBirth: string;
  login: string;
  password: string;
  roles: RoleName[];
}

export interface UpdateEmployeeRequest {
  name?: string;
  surname?: string;
  patronymic?: string;
  dateBirth?: string;
  roles?: RoleName[];
}
