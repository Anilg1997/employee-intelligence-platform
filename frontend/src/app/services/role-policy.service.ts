import { Injectable } from '@angular/core';
import { AppRole, AuthService } from './auth.service';

export const ROLE_POLICY = {
  audit: ['SUPER_ADMIN', 'HR_ADMIN'] as AppRole[],
  employeeMutations: ['SUPER_ADMIN', 'HR_ADMIN', 'HR_MANAGER'] as AppRole[],
  sensitiveInsights: ['SUPER_ADMIN', 'HR_ADMIN', 'HR_MANAGER', 'HR_ANALYST'] as AppRole[]
};

@Injectable({ providedIn: 'root' })
export class RolePolicyService {
  constructor(private readonly auth: AuthService) {}
  canAccess(roles: AppRole[]): boolean { return this.auth.hasAnyRole(roles); }
  canViewAudit(): boolean { return this.canAccess(ROLE_POLICY.audit); }
  canMutateEmployees(): boolean { return this.canAccess(ROLE_POLICY.employeeMutations); }
}
