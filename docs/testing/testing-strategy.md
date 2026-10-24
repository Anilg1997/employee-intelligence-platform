# Testing strategy

**Status: Existing and partially verified.** Unit and selected integration/security tests exist. Do not infer full system coverage or deployment readiness from a passing build.

## Current checks

| Area | Existing checks | Local command |
| --- | --- | --- |
| Angular | Vitest component/service tests and production build | `npm test -- --watch=false`; `npm run build` |
| Spring | JUnit/Mockito, PostgreSQL-backed Flyway integration, controller and secure authorization tests | `.\mvnw.cmd test` |
| ML | Pytest validation, schema, prediction, readiness, and JSON-artifact tests | `.\.venv\Scripts\python.exe -m pytest` |
| Compose | Configuration interpolation validation in CI | `docker compose config --quiet` with required environment values |
| Security workflow | CodeQL, Gitleaks, pip-audit, npm audit, Trivy config scan configured in Actions | Review actual workflow results; no local pass is implied |

The tests use isolated schemas for migration scenarios; do not reset the developer database to run them. The README records selected verification evidence and known gaps.

## Future acceptance layers

1. API contract tests for Angular ↔ Spring ↔ FastAPI request/response schemas.
2. Browser E2E for core routes, validation, error/retry, authorization denial, and responsive behavior.
3. Kafka integration tests for outbox retries, duplicate delivery, consumer idempotency, and DLQ replay.
4. RAG evaluation corpus with groundedness/source relevance, empty-context, tenant, and unavailable-model cases.
5. Performance/load tests with synthetic, non-sensitive fixtures and defined budgets.
6. Coverage/security gates only after baselines and supported tools are established; do not add thresholds that fail on unmeasured legacy code.
