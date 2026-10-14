# Bounded agent and MCP security

Agent requests are bounded by `agent.execution.max-tool-calls` (default 8) and
`agent.execution.timeout-seconds` (default 30, capped at 120). Each backend
agent tool checks the authenticated Spring role and records an
`AGENT_TOOL_CALL` audit event with the tool, subject, result, and correlation
context. Demo mode retains the existing local-development behavior; deployments
with authentication enabled fail closed for missing or unknown roles.

The MCP adapter has a deployment allowlist and role capability map, but these
are not a replacement for Spring authorization. It forwards the configured
bearer token to the existing API endpoints, allowing Spring to preserve actor
identity and endpoint role checks. `MCP_CORRELATION_ID` can be used to connect
adapter calls to audit records. The only additional tools are employee search,
workforce metrics, and report listing, each backed by an existing read endpoint.
