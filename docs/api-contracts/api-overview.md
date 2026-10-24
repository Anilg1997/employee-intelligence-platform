# API overview

**Status: Existing and verified by source inspection** for the endpoints below. Use the Spring OpenAPI configuration as the evolving contract; these tables are not a substitute for generated schemas.

## Spring Boot (`http://localhost:8080/api`)

| Method | Path | Status / notes |
| --- | --- | --- |
| GET | `/employees` | Existing employee listing; pagination/filter parameters should be checked in controller before client changes |
| GET | `/employees/search` | Existing paged employee search |
| GET | `/employees/{id}` | Existing employee detail |
| POST / PUT / DELETE | `/employees[/{id}]` | Existing CRUD; secure role policy applies |
| GET | `/dashboard/summary` | Existing aggregate dashboard response |
| POST | `/ml/predict` | Existing attrition prediction proxy |
| POST | `/ml/predict/salary` | Existing salary model proxy |
| POST | `/ml/predict/performance` | Existing performance model proxy |
| POST | `/ml/predict/promotion` | Existing rule-based demo evaluation |
| GET | `/ml/models` | Existing model registry response; not a deployment manager |
| GET | `/ai/employees/{employeeId}/prediction`, `/risk`, `/risk-summary` | Existing prediction/risk read endpoints |
| POST | `/ai/chat`, `/agent/ask` | Existing AI/agent endpoints; external tools must remain permission-gated |
| POST | `/rag/ask` | Existing RAG question request and answer/source response |
| POST | `/rag/ingestion` | Existing document ingestion endpoint; restricted in secure mode |
| POST / GET | `/rag/documents`, `/rag/documents/search` | Existing document operations |
| GET | `/audit/events` | Existing paginated audit query with action/result filters; secure mode is HR_ADMIN/SUPER_ADMIN only |

Request/response DTOs and validation are authoritative. Do not infer currency from synthetic dataset income units or treat predictions as outcomes.

## FastAPI (`http://127.0.0.1:8000`)

| Method | Path | Status |
| --- | --- | --- |
| GET | `/health` | Liveness |
| GET | `/ready` | Model availability/readiness |
| POST | `/predict` | Attrition prediction |
| POST | `/predict/salary` | Salary estimate |
| POST | `/predict/performance` | Performance class estimate |
| POST | `/predict/promotion` | Portfolio/demo rule score |

## Planned only

Identity, invitation, onboarding, HR case, analytics/reporting, notification, and separate knowledge/workflow service APIs are not implied by the current routes. See [service boundaries](../architecture/service-boundaries.md).
