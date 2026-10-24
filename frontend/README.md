# Angular frontend

This is the existing Angular 21 application. Keep current routes and components in place unless an approved refactor updates all route, import, and test references.

## Development

Use Node.js 22 compatible with Angular 21 and the committed npm lockfile:

```powershell
npm ci
npm start
```

The development UI is `http://localhost:4200`; Spring Boot defaults to `http://localhost:8080/api`. Supply public runtime API configuration through the existing `window.__APP_CONFIG__` mechanism when needed. It is not a secret store.

## Build and tests

```powershell
npm test -- --watch=false
npm run build
```

Tests use Vitest through Angular CLI. An end-to-end browser runner is not configured. Prettier settings are in `.prettierrc`; no Angular ESLint target is currently configured, so do not infer lint coverage from a successful build.

For complete service startup, environment configuration, and limitations, see the root [README](../README.md) and [Windows setup](../docs/setup/windows-development.md).
