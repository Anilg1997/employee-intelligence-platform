# Audit logging and AI governance

Phase 9 adds a small, durable audit trail backed by PostgreSQL and Flyway. Events capture **who** (the OIDC subject and one role), **what** action occurred, **which** resource was involved, the result, and a bounded metadata note. Event timestamps are UTC and the API supports read-only pagination with action and result filters.

## Data minimization

Audit metadata is intentionally operational: it must not contain prompts, model responses, access tokens, credentials, secrets, or unnecessary employee PII. The `AuditService` applies a defensive redaction and length limit, while callers pass only identifiers and outcome summaries. Audit write failures are logged and swallowed so an audit database problem never breaks an employee, ML, risk, or ingestion operation.

## Access and modes

`GET /api/audit/events` is available in local demo mode (`SECURITY_AUTH_ENABLED=false`) so the UI can be evaluated locally. In secure mode it requires `SUPER_ADMIN` or `HR_ADMIN`; use an OIDC token with the provider's `roles` claim. The Angular screen displays a clear local/demo banner when authentication is disabled.

## Covered operations

Employee create/update/delete, ML prediction calls, dashboard and employee risk summaries, and RAG ingestion write success events. Audit failures and rejected authorization requests should be treated as security telemetry gaps and monitored at deployment level; no audit record should be interpreted as proof that an operation was appropriate. AI outputs remain decision-support only and require human review.

## Retention and operations

Define retention, export, and deletion controls with the deployment's privacy owner. Restrict database access to the application role and protect audit exports because actor subjects and resource identifiers can still be sensitive. The schema intentionally has no Kafka or Redis dependency.
