import { inject, Injectable, signal } from '@angular/core';
import { AUTH_ENABLED_TOKEN } from '../config/auth.config';

export const APP_ROLES = ['SUPER_ADMIN', 'HR_ADMIN', 'HR_MANAGER', 'HR_ANALYST', 'EMPLOYEE'] as const;
export type AppRole = typeof APP_ROLES[number];
export interface AuthState { authenticated: boolean; token: string | null; roles: AppRole[]; expiresAt: number | null; demo: boolean; }

const TOKEN_KEY = 'oidc_access_token';
const DEMO_ROLES: AppRole[] = ['SUPER_ADMIN'];

export function parseJwtPayload(token: string): Record<string, unknown> | null {
  try {
    const part = token.split('.')[1];
    if (!part) return null;
    const normalized = part.replace(/-/g, '+').replace(/_/g, '/') + '='.repeat((4 - part.length % 4) % 4);
    const json = atob(normalized);
    const payload = JSON.parse(json);
    return payload && typeof payload === 'object' ? payload : null;
  } catch { return null; }
}
export function isTokenExpired(token: string, now = Date.now()): boolean {
  const exp = parseJwtPayload(token)?.['exp'];
  return typeof exp !== 'number' || exp * 1000 <= now;
}

function rolesFrom(payload: Record<string, unknown>): AppRole[] {
  const raw = [payload['roles'], payload['role'], payload['http://schemas.microsoft.com/ws/2008/06/identity/claims/role']]
    .flatMap(value => Array.isArray(value) ? value : typeof value === 'string' ? [value] : []);
  return [...new Set(raw.map(String).map(role => role.toUpperCase()).filter((role): role is AppRole => (APP_ROLES as readonly string[]).includes(role)))];
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly enabled = inject(AUTH_ENABLED_TOKEN);
  readonly state = signal<AuthState>(this.readState());
  readonly lastAuthError = signal<401 | 403 | null>(null);

  private readState(): AuthState {
    if (!this.enabled) return { authenticated: true, token: null, roles: DEMO_ROLES, expiresAt: null, demo: true };
    const token = typeof sessionStorage === 'undefined' ? null : sessionStorage.getItem(TOKEN_KEY);
    const payload = token ? parseJwtPayload(token) : null;
    const expiresAt = typeof payload?.['exp'] === 'number' ? payload['exp'] as number : null;
    const authenticated = !!token && !!payload && expiresAt !== null && expiresAt * 1000 > Date.now();
    return { authenticated, token: authenticated ? token : null, roles: authenticated ? rolesFrom(payload!) : [], expiresAt, demo: false };
  }
  isAuthenticated(): boolean {
    const current = this.readState();
    const previous = this.state();
    if (!current.authenticated && previous.token) this.clear();
    // Role checks also run from templates; avoid publishing unchanged state on every check.
    else if (current.token !== previous.token || current.authenticated !== previous.authenticated || current.expiresAt !== previous.expiresAt) this.state.set(current);
    return current.authenticated;
  }
  token(): string | null { return this.isAuthenticated() ? this.state().token : null; }
  hasAnyRole(roles: AppRole[]): boolean { return !this.enabled || (this.isAuthenticated() && roles.some(role => this.state().roles.includes(role))); }
  setAccessToken(token: string): void { if (this.enabled && typeof sessionStorage !== 'undefined') { sessionStorage.setItem(TOKEN_KEY, token); this.state.set(this.readState()); } }
  clear(): void { if (typeof sessionStorage !== 'undefined') sessionStorage.removeItem(TOKEN_KEY); this.state.set(this.readState()); }
  recordAuthError(status: 401 | 403): void { this.lastAuthError.set(status); if (status === 401) this.clear(); }
}
