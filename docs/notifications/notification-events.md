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

## Topics, groups, keys, and delivery

The source topic is `employee-intelligence.events` (`OUTBOX_KAFKA_TOPIC`). The
notification consumer uses group `notifications-v1` and the outbox publisher
uses the numeric outbox id as the Kafka record key. This keeps a single event's
retries ordered while allowing different events to partition independently.
`report.generated` is produced after a successful report save in that service's
transaction, and `prediction.completed` after each successful ML prediction
endpoint. The outbox row is persisted before the producer returns, so broker
availability does not determine the API result.

The publisher retries broker acknowledgements (`outbox.publisher.max-retries`)
with `outbox.publisher.retry-backoff-ms`; an unsuccessful row remains pending
and is retried by the scheduled publisher. Consumer processing uses
`notifications.consumer.max-retries` attempts with fixed
`notifications.consumer.retry-backoff-ms` backoff. After those attempts,
Spring Kafka publishes the original record to `<source-topic>.DLT` (suffix is
configured by `notifications.consumer.dead-letter-suffix`) and does not
acknowledge it if the DLT publication fails.

The admin replay endpoints are restricted to `SUPER_ADMIN` and `HR_ADMIN`, and
write an audit event. Failed outbox rows can be requeued at
`POST /api/admin/kafka/outbox/{id}/replay`. A validated notification envelope
from the configured DLT can be sent back to the source topic at
`POST /api/admin/kafka/dlq/replay`; arbitrary topics and non-notification
payloads are rejected.

## Audit separation

`OutboxService.enqueueAudit(...)` emits `AuditEvent` payloads with audit fields
such as `action`, `resourceType`, and `result`. They have no `schemaVersion`,
notification `eventType`, or `recipientSubject`, and are intentionally ignored
by `NotificationEventConsumer`.
