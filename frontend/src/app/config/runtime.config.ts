export interface RuntimeConfig {
  apiBaseUrl: string;
  authEnabled: boolean;
  oidcAuthority: string;
  oidcClientId: string;
  oidcRedirectUri: string;
  oidcPostLogoutRedirectUri: string;
  oidcScope: string;
}

declare global {
  interface Window { __APP_CONFIG__?: Partial<RuntimeConfig>; }
}

const defaults: RuntimeConfig = {
  apiBaseUrl: 'http://localhost:8080/api',
  authEnabled: false,
  oidcAuthority: '',
  oidcClientId: '',
  oidcRedirectUri: '',
  oidcPostLogoutRedirectUri: '',
  oidcScope: 'openid profile'
};

export const RUNTIME_CONFIG: RuntimeConfig = {
  ...defaults,
  ...(typeof window !== 'undefined' ? window.__APP_CONFIG__ : {})
};
