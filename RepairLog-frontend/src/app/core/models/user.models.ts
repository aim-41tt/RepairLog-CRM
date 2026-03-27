import { RoleName } from './auth.models';

export interface UserProfile {
  employeeId: number;
  fullName: string;
  roles: RoleName[];
}
