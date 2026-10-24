# Role and access matrix

**Status: Configured and partially verified** for the current Spring `secure` profile. This is not proof of a complete identity solution. The app has no finished frontend OIDC login/callback/refresh flow; local profile is permit-all and is for trusted development only.

| Capability | Employee | HR Manager | HR Analyst | HR Admin | Super Admin | Director |
| --- | --- | --- | --- | --- | --- | --- |
| Read employee directory/profile | Denied by current secure API rules | Allowed | Allowed | Allowed | Allowed | Not configured |
| Create/update employee | Denied | Allowed | Denied | Allowed | Allowed | Not configured |
| Delete employee | Denied | Allowed | Denied | Allowed | Allowed | Not configured |
| Dashboard and prediction/risk APIs | Denied | Allowed | Allowed | Allowed | Allowed | Not configured |
| RAG/AI chat | Denied | Allowed | Allowed | Allowed | Allowed | Not configured |
| Audit events, document ingestion | Denied | Denied | Denied | Allowed | Allowed | Not configured |
| Employee self-service / onboarding approval | Not implemented | Not implemented | Not implemented | Not implemented | Not implemented | Not implemented |

Current server authority is derived from the external JWT `roles` claim, prefixed with `ROLE_`; tenant identity is read from supported tenant claims. Route guards and UI visibility do not grant access. The secure audit API has controller/method restrictions and focused authorization tests. Full tenant isolation across all persistence paths still needs review.

“Director” appears in the requested future business roles but is not a configured Spring role. Do not assign it access until scope, data minimization, and authorization tests are defined.
