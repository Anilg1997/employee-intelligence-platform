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

`mcp-server/` is a separate Python adapter with an opt-in Compose profile. Its Spring base URL, optional bearer token, role capability filter, tool allowlist, and bounded timeouts are environment-configured. The role filter is not caller authentication: the configured bearer token is shared by the adapter, so caller-specific identity propagation, rate limiting, sensitive-field filtering, and tool-call auditing are still required before public or multi-user deployment. Do not grant shell, arbitrary filesystem, or unrestricted database access.

## Health and tests

Use Spring readiness at `http://localhost:8081/actuator/health/readiness` and FastAPI `/ready`. The MCP server starts after backend readiness in Compose; its client handshake and tool listing are the available protocol-level checks. Run existing RAG/agent tests before model-backed acceptance. Do not treat successful model startup as evidence of answer quality.
