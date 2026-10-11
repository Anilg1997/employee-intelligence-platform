/**
 * Frontend API configuration.
 *
 * Keep the origin in one place so local development and deployed builds can
 * change the backend location without changing individual services.
 */
import { RUNTIME_CONFIG } from './runtime.config';

export const API_BASE_URL = RUNTIME_CONFIG.apiBaseUrl;

export function apiUrl(path: string): string {
  return `${API_BASE_URL}/${path.replace(/^\//, '')}`;
}
