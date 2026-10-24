# Implementation roadmap

**Status: Planned, not implemented** as a committed delivery schedule. Dependencies and acceptance checks below prevent scaffolding from being mistaken for working features.

| Phase | Scope | Prerequisites | Acceptance |
| --- | --- | --- | --- |
| 0. Stabilize foundations | Reproduce builds, verify current DB history, security profiles, configuration and CI | Existing app + current migrations | CI is green; safe local start; no applied migration edits; documented known limitations |
| 1. Identity and access | Select an OIDC provider; complete Angular login/callback/refresh/logout; role/tenant policies | Issuer, client registration, claims contract, redirect URIs | Unauthorized and cross-tenant cases rejected by integration tests; no local-mode production path |
| 2. Employee lifecycle | Onboarding/offboarding, self-service, manager assignments and approvals | Identity claims, employee data model, retention decisions | State transitions, validation, approval, audit, retries tested; existing CRUD contracts preserved |
| 3. Knowledge/RAG governance | Document lifecycle, source access, retrieval quality/evaluation | Approved policies, source owners, tenant rules, Ollama capacity | Grounded citations and empty-context response; authorization, update/delete and evaluation suite |
| 4. Kafka operations | Stabilize event schema, publisher, consumers, idempotency and DLQ | Broker operational policy, event ownership and retention | Outbox delivery/recovery tested; duplicate/replay and lag observable; no PII over-sharing |
| 5. Analytics and workflows | Derived analytics, HR cases, reporting and human review | Validated data quality, workflow contracts and identity | Real-source metrics, audit trail, accessible UI; no fabricated outcomes |
| 6. Service extraction | Extract only boundaries with proven ownership/scale needs | Stable APIs/events, data migration plan, operational owners | Incremental migration, rollback strategy, SLOs and security tests |
| 7. Deployment readiness | Secrets, TLS/networking, backups, SRE dashboards, container/dependency scanning | Completed identity and service operations | Restore drill, secure deployment review, monitored SLOs and release gates |

Do not create placeholder runnable services for future phases. The current Spring Boot modular monolith remains the backend until a phase is approved and its prerequisites are met.
