# Database and data ownership

**Status: Existing and verified** for the current migration inventory. Migration scripts are append-only after application; this setup work does not alter them.

## Current schema ownership

| Tables / data | Current owner | Evidence / limitations |
| --- | --- | --- |
| `employee` | Spring Boot employee module | Created by Flyway V2; tenant column/index/backfill in V4 |
| `rag_documents` | Spring Boot RAG module | Created by V2; tenant scope added in V4; vector extension enabled in V1 |
| `audit_events` | Spring Boot audit module | Created by V3; tenant scope added in V4 |
| `outbox_events` | Spring Boot outbox module | Created by V5; audit records are written transactionally with outbox rows |
| Model artifacts | FastAPI repository files | JSON artifacts under `ml-service/models`; not in PostgreSQL |
| Identity, workflow, analytics read models | None | No corresponding schema ownership is claimed |

Flyway versions currently present are V1–V5 (the version-3 `.sql.bak` file is not a migration). Hibernate schema mode is `validate`; do not use `ddl-auto=update` to bypass migration mismatches.

## Future ownership

Identity owns identity/role records; Employee retains employee master data; Workflow owns cases/tasks; Knowledge owns document metadata/versions/embeddings; Audit owns immutable audit records; Analytics owns derived, rebuildable read models. These boundaries are planned and do not authorize creating tables now.

## Operational requirements

- Back up the target database and inspect `flyway_schema_history` before upgrading an existing deployment.
- Test upgrades on a restored copy, validate checksums and row counts, then apply approved append-only migrations.
- Use least-privilege service DB credentials and tenant predicates/constraints.
- Plan tested restore, retention, encryption, and access review. Never remove Compose volumes to resolve schema errors.
- `postgres-data` is persistent. `docker compose down` stops services without deleting it; do not use `down -v` for routine development.

## Local database

Compose uses `pgvector/pgvector:pg17`, a named volume, and `pg_isready`. The checked-in Compose default password is intentionally absent; set `POSTGRES_PASSWORD` privately. Existing developer databases may use independent credentials; Compose initialization variables do not change an already-created volume's role password.
