export interface RuntimeConfig {
  apiBaseUrl: string;
  mlApiBaseUrl: string;
  authEnabled: boolean;
  oidcAuthority: string;
  oidcClientId: string;
}

declare global {
  interface Window { __APP_CONFIG__?: Partial<RuntimeConfig>; }
}

const defaults: RuntimeConfig = {
  apiBaseUrl: 'http://localhost:8080/api',
  mlApiBaseUrl: 'http://127.0.0.1:8000',
  authEnabled: false,
  oidcAuthority: '',
  oidcClientId: ''
};

export const RUNTIME_CONFIG: RuntimeConfig = {
  ...defaults,
  ...(typeof window !== 'undefined' ? window.__APP_CONFIG__ : {})
};
