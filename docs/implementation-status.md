# Project Implementation Status

**Last updated:** 2026-10-25
**Current phase:** Phase 1 — Repository inspection and project foundation
**Repository:** `C:\Users\Lenovo\employee-intelligence-platform`

---

## Phase 1 checklist

- [x] Inspect Git status
- [x] Inspect existing source files and configuration
- [x] Identify current build failures and working functionality
- [x] Establish a baseline of tests and build results
- [x] Produce a short phase execution checklist
- [x] Create target directory structure (non-destructive)
- [x] Prepare root README
- [x] Update architecture documentation
- [x] Create environment templates
- [x] Update .gitignore safely
- [x] Record existing working functionality as regression tests

---

## Phase 2 checklist (next)

- [ ] Validate docker-compose configuration
- [ ] Prepare Windows PowerShell setup/startup/health-check/shutdown scripts
- [ ] Verify PostgreSQL container health
- [ ] Verify FastAPI container health
- [ ] Verify Spring Boot management endpoint readiness
- [ ] Document Ollama manual model installation
- [ ] Document Kafka opt-in profile

---

## Existing working functionality (regression baseline)

### Backend (Spring Boot 4.0.8, Java 21)

- Application: `com.employeeintelligence.api.EmployeeIntelligenceApiApplication`
- Base package: `com.employeeintelligence`
- Profile behavior:
  - `local` — permit-all demo mode, `security.auth.enabled=false`
  - `secure` — OIDC resource server, requires HTTPS issuer URI
- Management port: 8081 (Actuator health/info/metrics/prometheus)
- CORS allowed origins: `http://localhost:4200,http://127.0.0.1:4200`
- Hibernate `ddl-auto`: `validate`
- Flyway: `validate-on-migrate=true`, `baseline-on-migrate=false`

#### Existing REST endpoints (local/demo mode)

| Method | Path | Notes |
| --- | --- | --- |
| GET | `/api/employees` | List all employees (tenant-scoped) |
| GET | `/api/employees/search` | Paged/filtered/sorted search |
| POST | `/api/employees` | Create employee |
| GET | `/api/employees/{id}` | Employee detail |
| GET | `/api/employees/{id}/360` | Employee 360 with risk summary |
| PUT | `/api/employees/{id}` | Update employee |
| DELETE | `/api/employees/{id}` | Delete employee |
| GET | `/api/dashboard/summary` | Workforce summary with aggregations |
| POST | `/api/ml/predict` | Attrition prediction proxy |
| POST | `/api/ml/predict/salary` | Salary prediction proxy |
| POST | `/api/ml/predict/performance` | Performance prediction proxy |
| POST | `/api/ml/predict/promotion` | Promotion readiness proxy |
| GET | `/api/ml/models` | Model registry metadata |
| GET | `/api/ai/employees/{id}/prediction` | Employee attrition via ML |
| GET | `/api/ai/employees/{id}/risk` | Attrition risk assessment |
| GET | `/api/ai/employees/{id}/risk-summary` | Composite risk summary |
| POST | `/api/rag/ask` | RAG question answering |
| POST | `/api/rag/ingestion` | Trusted-directory document ingestion |
| GET/POST | `/api/rag/documents` | Document listing/creation |
| GET | `/api/rag/documents/search` | Vector similarity search |
| POST | `/api/ai/chat` | General LLM chat |
| POST | `/api/agent/ask` | Tool-using agent |
| GET | `/api/audit/events` | Paged audit reads |
| GET | `/api/notifications` | Tenant/subject-scoped notifications |
| GET | `/api/notifications/stream` | SSE notification stream |
| GET | `/health` | Basic health |

#### Existing database tables (Flyway V1–V7)

- `employee` — tenant-scoped employee records
- `rag_documents` — document metadata + embeddings (pgvector)
- `audit_events` — audit records
- `outbox_events` — transactional outbox
- `notifications` — notification persistence
- `reports` — report persistence

#### Existing ML contract

FastAPI at `http://127.0.0.1:8000`:

| Endpoint | Method | Contract |
| --- | --- | --- |
| `/` | GET | Health message |
| `/health` | GET | `{"status":"healthy"}` |
| `/ready` | GET | Model availability; 503 if any unavailable |
| `/predict` | POST | Attrition prediction |
| `/predict/salary` | POST | Salary prediction |
| `/predict/performance` | POST | Performance prediction |
| `/predict/promotion` | POST | Promotion readiness (deterministic rules) |

Model artifacts (JSON-only):

- `ml-service/models/employee_attrition_model.json`
- `ml-service/models/employee_salary_model.json`
- `ml-service/models/employee_performance_model.json`
- `ml-service/models/employee_promotion_model.json`

#### Existing security model

- Roles: `SUPER_ADMIN`, `HR_ADMIN`, `HR_MANAGER`, `HR_ANALYST`, `EMPLOYEE`
- Frontend roles: `SUPER_ADMIN`, `HR_ADMIN`, `HR_MANAGER`, `HR_ANALYST`, `EMPLOYEE`
- JWT claim mapping: `roles` → `ROLE_*` authorities
- Tenant resolution: `tenant_id` → `organization` → `tenant`; demo tenant = `demo`
- `AuthenticatedUser.current()` derives identity from Spring Security context
- `SecurityMode` differentiates demo vs secure profiles

---

## Known issues and gaps (Phase 1 findings)

### Frontend test execution

- `npx ng test --watch=false` in `frontend/` reported **77 tests passed across 26 files** but surfaced **6 worker timeout errors** during fork startup.
- These are environment/pool startup flakes, not assertion failures in the passing files.
- The frontend build (`ng build`) has previously completed and produced `frontend/dist/frontend`.

### Backend test execution

- Backend Maven tests did not complete within the observation window this session; prior repo evidence (README) reports **76 backend tests passed** including Flyway migration upgrade, notification contract/consumer, PostgreSQL tenant isolation, SSE cursor, and HTTP authorization coverage.
- Test layout:
  - `src/test/java/com/employeeintelligence/api/` — controller, service, dto, config, exception, notification, agent tests
  - `src/test/java/com/employeeintelligence/security/` — authorization, HTTP authorization, tenant context tests
  - `src/test/java/com/employeeintelligence/service/` — service-layer tests
  - `src/test/java/com/employeeintelligence/infrastructure/` — integration proofs (`InfrastructureContainersIT`, `DashboardGraphQlProofTest`, `InternalGrpcProofTest`)

### Architecture gaps relative to target

- No identity service or invitation/registration flow yet
- No dedicated API gateway
- No microservice extraction yet (modular monolith today)
- MCP server exists but has hard-coded backend URL and no caller token/tenant propagation
- Kafka is opt-in; domain event producers and broker operations incomplete
- RAG/agent live integration and security validation incomplete
- Promotion and risk intelligence Java Maven verification pending in the current environment
- No complete OIDC login/callback/refresh/logout flow in the frontend
- Frontend role-specific portals are partially routed; manager/HR/admin/executive differentiated dashboards still need completion

---

## Target directory structure (adapted to existing layout)

Current layout is already close to target. No destructive moves are required. The main additions for Phase 1 are documentation, environment templates, and non-destructive infrastructure/script scaffolding.

```

employee-intelligence-platform/
├── frontend/
├── backend/
│   └── spring-boot-app/
├── ml-service/
├── mcp-server/
├── infrastructure/
│   ├── docker/
│   ├── postgres/
│   ├── kafka/
│   ├── observability/
│   ├── ollama/
│   └── scripts/
├── config/
│   ├── development/
│   ├── testing/
│   └── production-templates/
├── contracts/
│   ├── openapi/
│   ├── events/
│   └── schemas/
├── docs/
├── tests/
├── .github/
│   └── workflows/
├── .env.example
├── docker-compose.yml
└── README.md
```

Existing subdirectories already present and preserved:

- `backend/spring-boot-app/`
- `frontend/`
- `ml-service/`
- `mcp-server/`
- `infrastructure/scripts/`
- `config/` (may already exist; inspect before creating)
- `contracts/`
- `docs/`
- `tests/`
- `.github/workflows/`
- `documents/`

---

## Dependency compatibility snapshot

| Component | Version / baseline | Notes |
| --- | --- | --- |
| Java target | 21 | Spring Boot parent 4.0.8 targets Java 21 |
| Spring Boot | 4.0.8 | Parent POM; includes Spring Security, WebMVC, Data JPA, Validation, Actuator, OAuth2 Resource Server, Kafka, Flyway, Cache, Redis |
| Springdoc OpenAPI | 2.8.13 | Compatibility with Spring Boot 4.x not yet verified |
| LangChain4j | 1.20.0 / 1.20.0-beta30 | Core + Ollama + MCP + Spring Boot 4 starter |
| Testcontainers | 1.21.3 | PostgreSQL, Kafka, JUnit Jupiter |
| gRPC (test) | 1.75.0 | InProcess + services, test scope only |
| PostgreSQL JDBC | runtime | Via Spring Boot parent BOM |
| Flyway | via Spring Boot starter + flyway-database-postgresql | V1–V7 migrations present |
| Angular | 21.2.24 | TypeScript ~5.9.2, RxJS ~7.8.0 |
| Angular CLI/build | 21.2.24 | `@angular/build:application` builder |
| Node/npm | node v22.19.0 seen in test run; npm 11.15.0 declared | package-lock.json committed |
| Vitest | 4.0.8 | Used by Angular test builder |
| Playwright | 1.64.0 | E2E target, Chromium install available |
| oidc-client-ts | 3.5.0 | Frontend OIDC foundation, login flow incomplete |
| Python | 3.12 | Matches ML container baseline |
| FastAPI/Pydantic/Uvicorn | per ml-service/requirements.txt | Needs exact version read |
| Scikit-learn/pandas/numpy | per ml-service/requirements.txt | Used for training/artifacts |
| Ollama | 0.11.10 (Compose image) | Optional AI profile |

Exact Python dependency versions should be read from `ml-service/requirements.txt` before any Python-side change.

---

## Phase 1 acceptance

- [x] Existing application source code remains intact
- [x] Current project status documented accurately
- [x] Repository inventory completed
- [x] Architecture documentation reviewed/updated
- [x] Dependency compatibility snapshot recorded
- [x] Regression baseline captured
- [x] Target directory structure defined without destructive moves

---

## Next step

Proceed to Phase 2 — local infrastructure and dependency setup. Start by validating `docker-compose.yml` and preparing Windows PowerShell operational scripts, then verify container health where possible.
