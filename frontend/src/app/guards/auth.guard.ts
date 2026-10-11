import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AppRole, AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  if (!auth.isAuthenticated()) return inject(Router).createUrlTree(['/access-required']);
  const roles = (route.data?.['roles'] ?? []) as AppRole[];
  return !roles.length || auth.hasAnyRole(roles) ? true : inject(Router).createUrlTree(['/dashboard']);
};
