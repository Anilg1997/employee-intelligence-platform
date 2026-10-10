# Employee Intelligence Platform

An employee-workforce intelligence platform with an Angular web application,
Spring Boot API, machine-learning prediction service, HR policy RAG, and an
MCP server for tool-based access.

## Current status

### Implemented

- Angular frontend with dashboard, employee directory, employee create/edit/delete,
  attrition prediction, and HR assistant screens.
- `/employees` routes to the combined `EmployeesPage` (employee form plus directory).
- Spring Boot API integration for employees, dashboard summary, AI risk assessment,
  ML prediction, and RAG questions.
- Standalone ML service in `ml-service/` using FastAPI and the persisted
  `models/employee_attrition_model.pkl` pipeline.
- Experimental salary prediction endpoint, Spring proxy, and Angular screen.
  Salary model v2 uses training-only fitting with deterministic 60/20/20 splits
  and records validation/test metrics and dataset provenance. It is not suitable
  for compensation decisions. See [salary model report](docs/ml/salary-model.md).
- MCP server in `mcp-server/` exposing employee lookup, attrition risk,
  HR-policy search, and department statistics tools.
- Automated frontend, ML, and backend test suites are present in their respective
  project directories.
- PostgreSQL and pgvector migrations are defined under the Spring Boot resources,
  and a local Docker Compose stack is available for PostgreSQL, ML, and backend.

### In progress

- Runtime deployment wiring and environment-specific service URLs.
- End-to-end validation across the Angular app, Spring Boot API, ML service, and MCP
  server when all services are running together.
- Production hardening such as authentication, authorization, observability, and
  operational configuration.

### Planned

- Role-based access controls and audit history for HR actions.
- Model monitoring and operational retraining workflows (an explicit salary
  training command and held-out evaluation report are available).
- Production deployment automation and managed secrets/configuration.

## Repository layout

| Path | Purpose |
| --- | --- |
| `frontend/` | Angular 21 web application |
| `backend/spring-boot-app/` | Spring Boot REST API and business services |
| `ml-service/` | FastAPI attrition prediction service and model |
| `mcp-server/` | MCP HTTP server and its Python dependency manifest |

## Run locally

### Frontend

```bash
cd frontend
npm install
npm start
```

Open `http://localhost:4200`. The frontend API origin is centralized in
`frontend/src/app/config/api.config.ts` and defaults to `http://localhost:8080/api`.

### ML service

```bash
cd ml-service
python -m pip install -r requirements.txt
python -m src.train_salary_model
uvicorn src.api:app --host 0.0.0.0 --port 8000
```

The ML container definition is `ml-service/Dockerfile`; it expects the trained
pipeline at `ml-service/models/employee_attrition_model.pkl`.
Salary inference requires `models/employee_salary_model.pkl`; it never trains
automatically. Missing/incompatible artifacts return HTTP 503. The explicit
training command also writes `models/employee_salary_model.json` with provenance
and evaluation results. Salary's synthetic-data test MAE is 840.65 and RMSE is
1123.93 dataset income units; test R² is 0.933409, not a real-world accuracy claim.

### MCP server

```bash
cd mcp-server
python -m pip install -r requirements.txt
python server.py
```

The MCP server defaults to port `8001` and calls the Spring Boot API at
`http://localhost:8080`.

### Spring Boot API

See `backend/spring-boot-app/` for the existing Maven application and its
application configuration. Start it before using the frontend, ML-backed features,
or MCP tools.

### Docker Compose foundation

The Phase 1 local stack can be validated with:

```bash
docker compose config
docker compose up --build
```

The stack includes PostgreSQL with pgvector, the FastAPI ML service, and the
Spring Boot API. Docker Desktop (or another Docker Engine) must be running for
the build and startup commands. The backend runs Flyway migrations on startup;
employee CSV import is disabled by default and can be enabled explicitly with
`EMPLOYEE_IMPORT_ENABLED=true` and `EMPLOYEE_IMPORT_FILE`.

## Checks

From `frontend/`:

```bash
npm run build
npm test -- --watch=false
```

From `ml-service/`:

```bash
pytest
```

The attrition model is an estimate to support HR analysis and should not replace
human judgment or employment decisions.
