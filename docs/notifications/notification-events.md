# Notification event contract

Notification delivery consumes only versioned notification events from the
outbox Kafka topic. Audit outbox records are a separate contract and must not
be treated as notifications.

## Version 1 envelope

```json
{
  "schemaVersion": 1,
  "eventType": "risk.detected",
  "eventId": "risk-result-123",
  "tenantId": "tenant-a",
  "recipientSubject": "user-123"
}
```

Required fields are text values except `schemaVersion`, which must be the
integer `1`:

| Field | Meaning |
| --- | --- |
| `schemaVersion` | Contract version; currently `1` |
| `eventType` | One of `risk.detected`, `prediction.completed`, `report.generated` |
| `eventId` | Stable source-event identity for idempotent ingestion |
| `tenantId` | Tenant owning the event |
| `recipientSubject` | Exact authenticated subject that may receive it |

The envelope deliberately has no `title` or `body`. The backend chooses fixed,
allowlisted display text for each event type. Producers must not put user-facing
prose into this event.

## Producer integration

Future domain services should construct a `NotificationEvent` and call
`OutboxService.enqueueNotification(...)` in the same transaction as the domain
change. The outbox publisher then transports the serialized envelope. No
current service invokes this method yet.

## Audit separation

`OutboxService.enqueueAudit(...)` emits `AuditEvent` payloads with audit fields
such as `action`, `resourceType`, and `result`. They have no `schemaVersion`,
notification `eventType`, or `recipientSubject`, and are intentionally ignored
by `NotificationEventConsumer`.
