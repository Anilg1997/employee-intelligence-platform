/**
 * Frontend API configuration.
 *
 * Keep the origin in one place so local development and deployed builds can
 * change the backend location without changing individual services.
 */
export const API_BASE_URL = 'http://localhost:8080/api';

export function apiUrl(path: string): string {
  return `${API_BASE_URL}/${path.replace(/^\//, '')}`;
}
