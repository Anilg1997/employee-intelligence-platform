# ADR 0001: Evolve the existing platform incrementally

- Status: Accepted development constraint; not a claim of completed deployment
- Date: 2026-10-11
- Scope: Nexora Workforce Intelligence & AI Platform

## Context

The repository already contains Angular, Spring Boot, FastAPI prediction code, a Python MCP adapter, PostgreSQL/pgvector persistence and model artifacts. The target platform is broader than the implementation. Rebuilding or extracting every proposed service immediately would risk regressions and add distributed-system complexity before the boundaries are established.

## Decision

Preserve the existing stack, employee dataset and working behavior. Treat Spring Boot as the current business-module host, with separate ML and MCP processes. Improve and verify module boundaries before extracting services for measured scaling, ownership or deployment needs.

Use the README's IMPLEMENTED / IN PROGRESS / PLANNED vocabulary. A source file, dependency, diagram or passing unit test is not evidence of a complete production workflow. Keep current topology separate from planned Kafka/Redis/gateway/observability additions.

Retain existing trained artifacts until a replacement format reproduces their behavior. A future JSON-only format must contain preprocessing and model parameters, validate its schema, and pass prediction-parity tests; merely embedding pickle bytes in JSON is not a safe conversion. Preserve the original dataset's provenance and do not manufacture promotion outcomes.

## Consequences

- Existing functionality and learning investment are retained.
- Individual deployments remain simpler while new boundaries mature.
- Shared-process/database coupling must be managed explicitly; it does not disappear because modules are named as services.
- Feature work proceeds in small phases with compilation, relevant tests, integration evidence and documentation updates.
- Local startup, migration and security defects take priority over introducing additional infrastructure.

## Alternatives considered

- **Rebuild with a new stack:** rejected; unnecessary disruption and duplication.
- **Immediately deploy all target microservices:** deferred; operational complexity is not justified by current evidence.
- **Describe the target as implemented:** rejected; interview and engineering documentation must distinguish intent from verified behavior.

## Revisit when

An established module requires independent scaling, failure isolation, ownership or release cadence. Record the concrete decision, measurements, migration plan, security boundary and failure behavior in a new ADR before extraction.
