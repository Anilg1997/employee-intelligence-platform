# Windows development setup

**Status: Configured and verified in parts.** These commands preserve existing local files and persistent database volumes. Run long-lived processes in separate PowerShell terminals.

## Check prerequisites

From the repository root:

```powershell
.\infrastructure\scripts\check-prerequisites.ps1
# Add -RequireDocker if using Compose.
.\infrastructure\scripts\check-prerequisites.ps1 -RequireDocker
```

Baseline versions: Java 21 target, Python 3.12, Node 22 compatible with Angular 21, npm 11.15.0 lockfile, PostgreSQL 17/pgvector, and Docker Compose v2 where needed.

## Environment and database

Never overwrite an existing root `.env`. Copy the safe template only when no file exists, then replace the password locally:

```powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
docker compose up -d postgres
docker compose ps
```

Check database readiness without printing configuration or secrets:

```powershell
docker compose exec postgres sh -c 'pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
```

If those variables are not loaded into the current shell, use the values from the private `.env` without displaying that file. Do not delete `postgres-data`.

## Run application processes

Terminal 1 — ML service:

```powershell
Set-Location ml-service
.\.venv\Scripts\python.exe -m uvicorn src.api:app --host 127.0.0.1 --port 8000
```

Terminal 2 — Spring Boot (Maven run goal defaults to local profile; packaged deployments remain secure by default):

```powershell
Set-Location backend\spring-boot-app
.\mvnw.cmd spring-boot:run
```

Terminal 3 — Angular:

```powershell
Set-Location frontend
npm ci
npm start
```

Terminal 4 — optional local Ollama:

```powershell
ollama serve
ollama pull llama3.2
ollama pull nomic-embed-text
```

Or use the Compose `ai` profile and set `OLLAMA_BASE_URL=http://ollama:11434` in the ignored `.env`. Pull models manually; startup does not download them.

## Optional Kafka

The single-node KRaft broker is development-only:

```powershell
docker compose --profile messaging up -d kafka
docker compose --profile messaging ps kafka
```

For a containerized backend to publish, set `OUTBOX_PUBLISHER_ENABLED=true` and `KAFKA_BOOTSTRAP_SERVERS=kafka:9092` in `.env`, then recreate the backend. Local JVM runs use `localhost:9092`. Publishing alone does not provide consumers or DLQ processing.

## Quality checks

```powershell
Set-Location frontend
npm test -- --watch=false
npm run build

Set-Location ..\backend\spring-boot-app
.\mvnw.cmd test

Set-Location ..\..\ml-service
.\.venv\Scripts\python.exe -m pytest

Set-Location ..
$env:POSTGRES_PASSWORD = 'compose-validation-only'
$env:SPRING_PROFILES_ACTIVE = 'local'
docker compose config --quiet
```

Use your own approved local database credentials for backend tests; never reuse the validation-only Compose value against an existing database.

## Stop without data loss

```powershell
docker compose stop
```

To remove containers/network but retain named data volumes, use `docker compose down` (not `docker compose down -v`).
