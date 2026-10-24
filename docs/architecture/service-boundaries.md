# Proposed service boundaries

**Status: Planned, not implemented** for independent services. The current running backend is a Spring Boot modular monolith; FastAPI and the MCP adapter are separate processes. Responsibilities below are future extraction boundaries, not deployable service claims.

| Boundary | Responsibility / non-responsibility | Proposed API | Data owner | Events | Access, health and observability |
| --- | --- | --- | --- | --- | --- |
| API Gateway/BFF | Routing, edge policy and aggregation; no employee-domain ownership | Versioned external `/api/**` | None | Correlation metadata only | OIDC validation, rate limits, request IDs, health and access metrics |
| Identity | Identity lifecycle, invitation, credential/session integration and role assignment; no employee master records | `/identity/users`, `/identity/invitations`, OIDC integration | Identity store, not present | `InvitationAccepted` | Strong auth, admin workflows, audit, readiness and security events |
| Employee | Employee profile, department, reporting relationship and lifecycle; no model inference | `/employees/**` | Existing `employee` table stays with current Spring app until a migration is approved | `EmployeeCreated`, `EmployeeUpdated` | Tenant-scoped role checks, audit, readiness, latency and data-quality metrics |
| Analytics | Aggregates and reporting read models; not source employee records | `/analytics/**` | Derived read model, not present | Consumes employee lifecycle events | Aggregate-only authorization, freshness and lag metrics |
| Workflow | Onboarding/offboarding tasks, HR cases and approvals; not an identity provider | `/onboarding/**`, `/workflows/**`, `/hr-cases/**` | Workflow store, not present | `OnboardingTaskCompleted`, `HRCaseCreated` | Subject/manager/HR policy checks, workflow audit, queue health |
| ML integration | Validated orchestration to the existing FastAPI model service; not model training | `/predictions/**` | No independent data store | `PredictionGenerated` (proposed) | Minimize employee fields, role checks, model/service readiness and latency |
| Knowledge | Policy document ingestion, versioning, embeddings and retrieval; not answer generation | `/knowledge/documents/**`, `/knowledge/search` | Current `rag_documents` remains with Spring | `PolicyDocumentUpdated` | HR-admin ingestion, tenant scope, source provenance, ingestion health |
| Assistant | Local model orchestration, grounded answers and controlled tool routing; not an authorization authority | `/assistant/chat` | No conversation store currently claimed | Optional answer metadata; no event contract finalized | Check caller permissions before tools; model, retrieval and timeout metrics |
| Audit | Append-only access/business audit and query; not a general log sink | `/audit/events` | Current `audit_events` and `outbox_events` remain with Spring | Audit outbox exists; consumer contract unfinished | HR admin/super admin query, tenant filter, integrity and delivery-lag monitoring |
| MCP server | Narrow tool adapter for approved capabilities; not arbitrary shell/filesystem/database access | MCP tool protocol | No owned database | No event ownership | Explicit principal/tool authorization, least privilege, timeout, per-tool audit and health |

**Extraction gate:** identify a scaling, ownership, compliance, or deployment need; define API/event contracts and data ownership; add backward-compatible migration and authorization tests; then move one boundary without changing existing client contracts.
