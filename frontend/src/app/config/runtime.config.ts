export interface RuntimeConfig {
  apiBaseUrl: string;
  authEnabled: boolean;
  oidcAuthority: string;
  oidcClientId: string;
}

declare global {
  interface Window { __APP_CONFIG__?: Partial<RuntimeConfig>; }
}

const defaults: RuntimeConfig = {
  apiBaseUrl: 'http://localhost:8080/api',
  authEnabled: false,
  oidcAuthority: '',
  oidcClientId: ''
};

export const RUNTIME_CONFIG: RuntimeConfig = {
  ...defaults,
  ...(typeof window !== 'undefined' ? window.__APP_CONFIG__ : {})
};
