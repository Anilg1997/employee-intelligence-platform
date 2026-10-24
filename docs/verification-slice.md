# Infrastructure verification slice

This slice adds opt-in checks without changing the default local test path.

## Backend checks

```powershell
cd backend/spring-boot-app
./mvnw test
./mvnw -Pintegration verify
```

`mvn test` runs the existing unit/context checks and the GraphQL and in-process
gRPC proof tests. `-Pintegration verify` runs `InfrastructureContainersIT`,
which starts PostgreSQL, Kafka, and Redis with Testcontainers and performs a
connection/message smoke check. Docker Engine must be running; when it is not,
the command is expected to be unavailable and is not evidence of runtime
health. The test is intentionally named `*IT` and is excluded from the normal
local lifecycle.

The GraphQL endpoint is available at `/graphql` and exposes only the
tenant-scoped dashboard aggregate in this proof-of-value. REST remains the
primary API. The GraphQL schema is not a claim that all platform operations
should move to GraphQL.

The gRPC check is deliberately an internal proof-of-value only: it uses an
in-process health contract and deadline to prove that a typed internal RPC can
compile and be tested. No gRPC production server or public API is claimed until
there is a concrete worker boundary and protobuf contract to own.

## Browser smoke checks

```powershell
cd frontend
npm install
npm run e2e:install
npm run e2e
```

Playwright starts Angular on port 4200, stubs the dashboard and employee APIs,
and checks dashboard success, dashboard retry/error recovery, and the employee
directory route. These checks prove critical navigation and rendering while
keeping external services out of the browser test. They do not certify a live
backend, OIDC provider, or production deployment.

In CI, run `npm run e2e:install --with-deps` on a runner that supports the
Playwright browser. A future full-stack job should start Compose and replace
the route stubs only after seeded, non-production data and authentication are
available.
