
import { RUNTIME_CONFIG } from './runtime.config';

export const ML_API_BASE_URL = RUNTIME_CONFIG.mlApiBaseUrl;

export function mlApiUrl(path: string): string {
  return `${ML_API_BASE_URL}/${path.replace(/^\/+/, '')}`;
}
