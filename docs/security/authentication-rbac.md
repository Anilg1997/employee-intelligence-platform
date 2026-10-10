# Authentication and RBAC foundation

Phase 8 adds an integration seam for external OIDC/OAuth2 authentication and
role-based authorization. It does **not** claim that production OAuth is
configured, and it does not add a password store or homemade login flow.

## Local demo (default)

The backend defaults to the `local` profile and `security.auth.enabled=false`.
The HTTP filter chain permits existing demo routes, while method annotations
retain the intended authorization policy for secure deployments. The Angular
guard and interceptor are no-ops in the same mode, so existing local workflows
continue to work.

## Secure deployment seam

Run the backend with `SPRING_PROFILES_ACTIVE=secure`,
`SECURITY_AUTH_ENABLED=true`, and an external
`SECURITY_OAUTH2_ISSUER_URI`. Spring Security's resource-server JWT support
validates bearer tokens against that issuer. Configure the identity provider to
emit a `roles` claim containing values such as `HR_ADMIN`; the backend maps
these to `ROLE_HR_ADMIN`. No
secret, client credential, or token belongs in this repository.

Defined application roles are `SUPER_ADMIN`, `HR_ADMIN`, `HR_MANAGER`,
`HR_ANALYST`, and `EMPLOYEE`. High-impact employee deletion, RAG ingestion,
and model-registry reads have method-level checks; local demo mode explicitly
bypasses those checks. Expand the policy only with a reviewed business rule.

The frontend files `config/auth.config.ts`, `services/auth.service.ts`,
`guards/auth.guard.ts`, and `interceptors/auth.interceptor.ts` provide the
OIDC callback/token integration seam. Replace the build-time switch and connect
an approved OIDC client library in a deployment; this foundation intentionally
does not implement login UI.
