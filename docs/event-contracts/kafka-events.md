# Kafka event contracts

**Status: Configured and partially implemented.** PostgreSQL outbox persistence, audit-outbox writes, and an opt-in publisher exist. Kafka is available as an optional local Compose profile. Business consumers, retry topics, DLQ, replay tooling, schema registry, and end-to-end delivery guarantees are **planned, not implemented**.

## Proposed envelope

```json
{
  "eventId": "stable-unique-id",
  "eventType": "EmployeeCreated",
  "schemaVersion": 1,
  "occurredAt": "2026-01-01T12:00:00Z",
  "tenantId": "tenant-id",
  "correlationId": "request-correlation-id",
  "aggregateType": "employee",
  "aggregateId": "opaque-id",
  "payload": {}
}
```

Example values above are illustrative only. Events must not include unnecessary personal, compensation, or prediction fields. Partition by tenant and aggregate ID when ordering per aggregate is required.

## Proposed initial event names

| Event | Producer / consumers | Status |
| --- | --- | --- |
| `EmployeeCreated` | Employee module; analytics/audit | Planned |
| `EmployeeUpdated` | Employee module; analytics/audit | Planned |
| `InvitationAccepted` | Identity; onboarding | Planned |
| `OnboardingTaskCompleted` | Workflow; analytics/audit | Planned |
| `HRCaseCreated` | Workflow; authorized notification | Planned |
| `PredictionGenerated` | ML integration; governance/audit | Planned |
| `PolicyDocumentUpdated` | Knowledge ingestion; assistant index | Planned |
| `AuditEvent` | Current audit outbox publisher | Exists in current implementation; verify consumer contract before relying on it |

## Delivery requirements before production use

- Persist business state and outbox row in one transaction.
- Publish with stable event ID and idempotent producer settings; assume at-least-once delivery.
- Consumers deduplicate by event ID and apply tenant-aware authorization.
- Classify transient/permanent failures; bound retries and route poison events to a monitored DLQ.
- Define replay, retention, schema compatibility, PII minimization, correlation, alerting, and operator access.
- Preserve events while Kafka is unavailable and monitor outbox age/attempts.

## Local broker

The optional `messaging` Compose profile uses a single-node KRaft broker for development only. Start with `docker compose --profile messaging up -d kafka`. It is not a production cluster and does not enable publisher/consumer behavior by itself. See [Windows setup](../setup/windows-development.md).
