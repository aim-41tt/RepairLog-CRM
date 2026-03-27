import { CanActivateChildFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { inject } from '@angular/core';
import { UserService } from '../services/user.service';
import { RoleName } from '../models/auth.models';

export const roleGuard: CanActivateChildFn = (route: ActivatedRouteSnapshot) => {
  const user = inject(UserService);
  const router = inject(Router);

  // Walk up to find data.roles (may be on parent route)
  let r: ActivatedRouteSnapshot | null = route;
  let allowedRoles: RoleName[] | undefined;
  while (r) {
    if (r.data?.['roles']) { allowedRoles = r.data['roles']; break; }
    r = r.parent;
  }

  if (!allowedRoles) return true;
  const hasRole = allowedRoles.some(role => user.hasRole(role));
  return hasRole ? true : router.createUrlTree(['/menu']);
};
