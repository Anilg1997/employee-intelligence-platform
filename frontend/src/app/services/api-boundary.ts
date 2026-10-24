import { API_BASE_URL } from '../config/api.config';

/** Only attach application bearer credentials within the configured API path. */
export function isApplicationApi(url: string): boolean {
  const origin = typeof window === 'undefined' ? 'http://localhost' : window.location.origin;
  try {
    const base = new URL(API_BASE_URL, origin);
    const target = new URL(url, origin);
    const prefix = base.pathname.replace(/\/$/, '');
    return target.origin === base.origin && !target.username && !target.password &&
      (target.pathname === prefix || target.pathname.startsWith(prefix + '/'));
  } catch { return false; }
}
