import { RUNTIME_CONFIG } from './runtime.config';

/** Authentication is deliberately disabled by default for local demo mode. */
export const AUTH_ENABLED = RUNTIME_CONFIG.authEnabled;
export const OIDC_AUTHORITY = RUNTIME_CONFIG.oidcAuthority;
export const OIDC_CLIENT_ID = RUNTIME_CONFIG.oidcClientId;
