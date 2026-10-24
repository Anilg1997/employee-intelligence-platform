# Local AI, RAG, and MCP setup

**Status: In progress** for existing Spring/LangChain4j, Ollama, PostgreSQL/pgvector, and Python MCP code. A local inference path is configured; full quality, identity propagation, tool authorization, and production readiness are not verified.

## Ollama

Install/run Ollama locally, or use the optional Compose `ai` profile. Model files are not pulled during startup:

```powershell
ollama pull llama3.2
ollama pull nomic-embed-text
ollama list
```

For a host-installed runtime, Spring's default base URL is `http://localhost:11434`. In Compose, the backend defaults to `http://host.docker.internal:11434`. To use the optional container, set `OLLAMA_BASE_URL=http://ollama:11434` in the ignored `.env`, then run `docker compose --profile ai up -d ollama`. Pull models manually with `docker compose exec ollama ollama pull llama3.2` and `... ollama pull nomic-embed-text`. Keep Ollama private; the optional profile publishes no host port.

Model choice and memory needs depend on hardware. Verify compatible tags and available disk/RAM before downloading. No paid external AI API is required by the documented local setup.

## RAG

Spring owns ingestion/retrieval in the current application. PostgreSQL with pgvector stores embeddings and metadata. Approved files are mounted read-only at `/app/documents` in Compose; local backend defaults to `./documents`. Ingestion is a restricted operation in secure mode.

Expected answer contract includes source references. Evaluate source relevance, unsupported-answer behavior, tenant separation, deletion/version updates, latency, and model failure before calling the assistant production-ready. Empty retrieval must be surfaced rather than answered from invented company policy.

## MCP

`mcp-server/` is a separate Python adapter. It is not an identity provider or authorization bypass. Before exposure, replace hard-coded service configuration, propagate authenticated identity safely, allowlist each tool, minimize returned fields, enforce per-tool authorization/timeouts, and audit tool calls. Do not grant shell, arbitrary filesystem, or unrestricted database access.

## Health and tests

Use Spring readiness at `http://localhost:8081/actuator/health/readiness`, FastAPI `/ready`, and the MCP health behavior only after confirming its current implementation. Run existing RAG/agent tests before model-backed acceptance. Do not treat successful model startup as evidence of answer quality.
