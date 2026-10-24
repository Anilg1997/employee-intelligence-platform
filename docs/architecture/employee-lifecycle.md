# Employee lifecycle

**Status: Planned, not implemented** as an end-to-end lifecycle workflow. Existing employee CRUD/search and profile views do not constitute onboarding, self-service, manager approvals, or offboarding.

## Proposed flow

1. HR administrator creates a pending employee record or an invitation.
2. Identity provider verifies the person and accepts the invitation. The identity service is not implemented; do not create a password or claim a login workflow exists.
3. HR validates required fields, effective date, department, job role, manager and tenant. Persist through a versioned migration/API contract.
4. Workflow service creates onboarding tasks for the employee, manager, and HR. Each task has an owner, due date, state, and auditable transition.
5. Manager/HR approvals are authorized server-side. Client-side route guards are UX only.
6. Employee status becomes active only after required approvals. Publish an idempotent lifecycle event through a transactional outbox after the database change.
7. Offboarding records effective date, access-revocation request, asset/knowledge-transfer tasks, and retention disposition. Never hard-delete solely to simplify a workflow.

## Data and controls

The current `employee` table is owned by the Spring Boot application and is not a workflow engine. Future workflow state should have explicit ownership and retention rules. Events should carry stable IDs, tenant, correlation, event type/version, and only necessary fields—not a full employee record.

## Acceptance criteria for a future implementation

- Authorized roles can create and advance a lifecycle case; unauthorized callers receive 401/403.
- Required fields, duplicate identity, invalid dates, and tenant mismatches fail explicitly.
- State transitions are transactional, idempotent, auditable, and tested for retries.
- Employees see only their own permitted data; managers see only assigned reports.
- No invitation is considered accepted until identity-provider verification succeeds.
