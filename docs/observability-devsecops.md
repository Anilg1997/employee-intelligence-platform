# Observability and DevSecOps slice

## What is wired

The Spring Boot process exposes `health`, `info`, `metrics`, and `prometheus` on
the separate management port (`8081` by default). The existing HTTP correlation
filter bounds and returns `X-Correlation-Id`; keep that value in structured log
fields and pass it to downstream calls. It is an audit/log correlation aid, not
a distributed trace ID.

The following low-cardinality counters are emitted when the relevant code path
is present:

| Metric | Tags | Meaning |
| --- | --- | --- |
| `platform_ml_requests_total` | `model` | ML proxy requests by supported model |
| `platform_ml_failures_total` | `model`, `reason` | Upstream ML failures |
| `platform_rag_searches_total` | none | Hybrid retrieval attempts |
| `platform_rag_results_total` | `outcome=empty\|matched` | Retrieval result state |
| `platform_kafka_outbox_events_total` | `outcome=claimed\|published\|failed` | Opt-in outbox publisher activity |
| `platform_reports_generated_total` | `type` | Existing deterministic report generation |

Do not add tenant IDs, employee IDs, prompts, document text, model input
features, or arbitrary exception text as metric labels. Use logs and the audit
record for bounded identifiers, with the correlation ID, operation, actor,
tenant, result, and failure class; never log bearer tokens, raw prompts, HR
fields, or retrieved document contents. Audit persistence remains the durable
record and must not be replaced by application logs.

## CI and security gates

`.github/workflows/ci.yml` runs the repository's Maven, pytest, and Angular
checks, uploads coverage artifacts when produced, builds the ML and Spring
images, and scans those images with Trivy for unfixed HIGH/CRITICAL findings.
`.github/workflows/security.yml` continues CodeQL, secret/dependency auditing,
and configuration scanning. Coverage is reported as an artifact; this slice
does not claim a percentage threshold.

There is intentionally no automatic DAST job: the repository does not provide
a verified, safely deployed CI target with seeded non-production data and an
approved base URL. Add a manually reviewed DAST workflow only after those
external controls exist; do not point a scanner at production.

## Required external services and verification limits

- Java integration/startup checks require reachable PostgreSQL with pgvector;
  tenant isolation tests use isolated schemas.
- RAG and agent calls require a compatible Ollama endpoint and models.
- ML CI tests use local artifacts; they do not prove a live deployment.
- Kafka publishing/consumer checks require a broker and explicit opt-in
  configuration (`OUTBOX_PUBLISHER_ENABLED` and consumer settings). Kafka is
  not in the default Compose stack.
- MCP verification requires a running Spring API, trusted caller identity and
  configured tool policy. The adapter must not be treated as authentication.
- Container build/health and Kubernetes rollout require Docker/Kubernetes
  infrastructure; Compose config validation alone is not runtime evidence.
