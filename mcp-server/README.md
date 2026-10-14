# Employee Intelligence MCP adapter

The existing tools and `MCPServer` HTTP startup are preserved. The adapter also
exposes `search_employees`, `get_workforce_metrics`, and `list_reports`, mapped
only to existing Spring endpoints. Configure
the adapter through deployment environment variables (never tool arguments):

| Variable | Default | Behavior |
| --- | --- | --- |
| `SPRING_API_BASE_URL` | `http://localhost:8080` | Fixed trusted Spring API origin/base path; HTTP(S), no credentials, query or fragment. Use HTTPS for remote deployments. |
| `SPRING_API_BEARER_TOKEN` | unset | Forwarded as `Authorization: Bearer …`; absent means no authorization header. Supply using deployment secrets. |
| `MCP_CORRELATION_ID` | unset | Optional validated value forwarded as `X-Correlation-Id` for Spring audit correlation. |
| `MCP_ROLE` | `employee` | `employee`: HR policy only. `manager`, `hr_admin`, `admin`: all four tools. Unknown/empty roles deny all. |
| `MCP_ALLOWED_TOOLS` | all tool names | Comma-separated intersection with role permissions. Explicitly empty denies all; unknown names never grant access. |
| `MCP_EMPLOYEE_TIMEOUT_SECONDS` | 10 | Request timeout, clamped to 0.1–30 seconds. |
| `MCP_RISK_TIMEOUT_SECONDS` | 20 | Same bounds. |
| `MCP_POLICY_TIMEOUT_SECONDS` | 20 | Same bounds. |
| `MCP_STATISTICS_TIMEOUT_SECONDS` | 10 | Same bounds. |

Invalid/nonfinite timeout settings use the corresponding default. Requests uses
these bounds for connection/read inactivity; this is not an overall wall-clock
deadline for a streaming response. Redirects are disabled. Non-2xx responses and
transport exceptions return fixed errors without upstream bodies or exception
details. Successful response bodies retain the existing tool contract.

Employee IDs must be actual integers in 1–2147483647 (booleans and strings are
rejected). Policy questions must be nonblank strings of at most 2000 characters,
without control characters except tab/newline/carriage return.

The configured role is a capability filter only; Spring remains the authoritative
identity and authorization boundary. The bearer token is forwarded unchanged,
so Spring derives the actor identity and applies endpoint role checks. Tool
callers cannot supply roles, URLs, or tokens. Correlation IDs are optional and
validated before forwarding.
The server continues to bind to loopback port 8001.

## Tests

Install `pytest` and `requests`, then run:

```powershell
python -m pytest test_server.py -q
```

The adapter unit tests mock HTTP and the existing MCP registration decorator.
They do not verify the installed MCP SDK or HTTP transport. `test_client.py` and
`test_http_client.py` remain manual client scripts requiring the actual SDK and
running services; they are not unit tests. The original `MCPServer` API/framework
has not been migrated.
