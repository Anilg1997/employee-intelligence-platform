# Environment variables

**Status: Configured and partially verified.** Names below are based on current Spring properties and Compose configuration. Do not put credentials in tracked files. [`.env.example`](../../.env.example) contains placeholders only.

| Variable | Consumer | Default / requirement |
| --- | --- | --- |
| `POSTGRES_DB`, `POSTGRES_USER` | Compose and Spring | `employee_intelligence`, `postgres` |
| `POSTGRES_PASSWORD` | Compose and Spring | Required from private `.env`/environment; no real value is documented |
| `SPRING_PROFILES_ACTIVE` | Compose/deployment | Compose requires explicit `local` or `secure`; local is unauthenticated |
| `SECURITY_OAUTH2_ISSUER_URI` | Secure Spring profile | Required approved issuer; do not set a fake production issuer |
| `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | Spring | Override the local JDBC defaults; inject credentials privately |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Spring | `validate`; do not set `update` to bypass Flyway |
| `SPRING_FLYWAY_BASELINE_ON_MIGRATE` | Spring | `false`; baseline only after schema/history review |
| `SERVER_PORT`, `MANAGEMENT_SERVER_PORT` | Spring | `8080`, `8081` |
| `CORS_ALLOWED_ORIGINS` | Spring | Exact local Angular origins by default |
| `ML_SERVICE_URL` | Spring | `http://127.0.0.1:8000` local; `http://ml-service:8000` in Compose |
| `ML_SERVICE_PORT`, `BACKEND_PORT`, `BACKEND_MANAGEMENT_PORT`, `POSTGRES_PORT` | Compose host binding | `8000`, `8080`, `8081`, `5432`; loopback-bound |
| `MCP_SERVER_PORT`, `MCP_HOST`, `MCP_PORT` | MCP/Compose | `8001`; MCP is opt-in with the `mcp` profile and host publication stays loopback-bound |
| `MCP_ROLE`, `MCP_ALLOWED_TOOLS` | MCP | `employee` policy-only capability filter by default; intersecting allowlist; neither represents caller identity or grants Spring authorization |
| `SPRING_API_BASE_URL`, `SPRING_API_BEARER_TOKEN` | MCP | Compose uses `http://backend:8080`; optional forwarded bearer token must be supplied privately for secure Spring mode and must be authorized for each enabled endpoint |
| `EMPLOYEE_IMPORT_ENABLED`, `EMPLOYEE_IMPORT_FILE` | Spring/Compose | Disabled, empty path by default |
| `RAG_INGESTION_DIRECTORY`, `RAG_INGESTION_MAX_FILE_SIZE_BYTES`, `RAG_INGESTION_ALLOWED_EXTENSIONS` | Spring | Local documents path, bounded size, restricted file extensions |
| `OLLAMA_BASE_URL` | Spring | Host-local default; use `http://ollama:11434` for Compose AI profile |
| `OLLAMA_CHAT_MODEL`, `OLLAMA_EMBEDDING_MODEL`, `OLLAMA_CHAT_TEMPERATURE` | Spring | `llama3.2`, `nomic-embed-text`, `0.7` |
| `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_PORT` | Spring/Compose | `kafka:9092` in Compose, `localhost:9092` on host |
| `OUTBOX_PUBLISHER_ENABLED`, `OUTBOX_KAFKA_TOPIC`, `OUTBOX_PUBLISHER_BATCH_SIZE`, `OUTBOX_PUBLISHER_MAX_RETRIES`, `OUTBOX_PUBLISHER_ACK_TIMEOUT_MS`, `OUTBOX_PUBLISHER_RETRY_BACKOFF_MS`, `OUTBOX_PUBLISHER_FIXED_DELAY_MS` | Spring | Publisher disabled by default; bounded defaults in `application.properties` |
| `CACHE_REDIS_ENABLED`, `REDIS_URL`, `SPRING_CACHE_TYPE` | Spring | Redis cache opt-in; Redis is not in default Compose |
| `window.__APP_CONFIG__` | Angular browser | Public API/auth settings only; never store a client secret here |

Spring imports the ignored root `.env` when launched from `backend/spring-boot-app/`. Docker Compose reads root `.env`. Python does not automatically load it. Environment overrides take precedence. Verify the actual property definitions before adding variables to deployment tooling.
