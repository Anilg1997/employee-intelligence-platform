import { Injectable } from '@angular/core';
import { AUTH_ENABLED } from '../config/auth.config';

export type AppRole = 'SUPER_ADMIN' | 'HR_ADMIN' | 'HR_MANAGER' | 'HR_ANALYST' | 'EMPLOYEE';

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly enabled = AUTH_ENABLED;
  isAuthenticated(): boolean { return !this.enabled || this.token() !== null; }
  token(): string | null { return !this.enabled || typeof sessionStorage === 'undefined' ? null : sessionStorage.getItem('oidc_access_token'); }
  /** OIDC callback integration seam; this app does not implement password login. */
  setAccessToken(token: string): void { if (this.enabled && typeof sessionStorage !== 'undefined') sessionStorage.setItem('oidc_access_token', token); }
  clear(): void { if (typeof sessionStorage !== 'undefined') sessionStorage.removeItem('oidc_access_token'); }
}
