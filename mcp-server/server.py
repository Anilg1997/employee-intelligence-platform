"""Controlled MCP adapter for the Employee Intelligence Spring API."""

import asyncio
import math
import os
from typing import Any, Callable
from urllib.parse import urlparse

import requests
from mcp.server import MCPServer


mcp = MCPServer("Employee Intelligence MCP")

_TOOL_NAMES = {
    "get_employee",
    "assess_attrition_risk",
    "search_hr_policy",
    "get_department_statistics",
    "search_employees",
    "get_workforce_metrics",
    "list_reports",
}
_ROLE_TOOLS = {
    "employee": {"search_hr_policy"},
    "manager": _TOOL_NAMES,
    "hr_admin": _TOOL_NAMES,
    "admin": _TOOL_NAMES,
}
_DEFAULT_TIMEOUTS = {
    "employee": 10.0,
    "risk": 20.0,
    "policy": 20.0,
    "statistics": 10.0,
    "search": 10.0,
    "reports": 10.0,
}
_MAX_TIMEOUT = 30.0
_MAX_EMPLOYEE_ID = 2_147_483_647
_MAX_QUESTION_LENGTH = 2_000


class AdapterValidationError(ValueError):
    """Only fixed, safe messages may be used with this exception."""


def _error(message: str) -> str:
    """Return a stable, deliberately non-sensitive tool error."""
    return f"Error: {message}"


def _base_url() -> str:
    value = os.getenv("SPRING_API_BASE_URL", "http://localhost:8080").strip().rstrip("/")
    try:
        parsed = urlparse(value)
        valid = (
            parsed.scheme in {"http", "https"} and parsed.hostname
            and not parsed.username and not parsed.password
            and not parsed.query and not parsed.fragment
            and not any(char.isspace() or ord(char) < 32 for char in value)
        )
        parsed.port  # Validate malformed and out-of-range ports.
    except ValueError:
        valid = False
    if not valid:
        raise AdapterValidationError("SPRING_API_BASE_URL is invalid")
    return value


def _timeout(name: str) -> float:
    raw = os.getenv(f"MCP_{name.upper()}_TIMEOUT_SECONDS")
    if raw is None:
        return _DEFAULT_TIMEOUTS[name]
    try:
        value = float(raw)
    except (TypeError, ValueError):
        return _DEFAULT_TIMEOUTS[name]
    if not math.isfinite(value):
        return _DEFAULT_TIMEOUTS[name]
    return max(0.1, min(value, _MAX_TIMEOUT))


def _headers() -> dict[str, str]:
    token = os.getenv("SPRING_API_BEARER_TOKEN", "").strip()
    if any(char.isspace() or ord(char) < 32 or ord(char) > 126 for char in token):
        raise AdapterValidationError("SPRING_API_BEARER_TOKEN is invalid")
    headers = {"Authorization": f"Bearer {token}"} if token else {}
    correlation = os.getenv("MCP_CORRELATION_ID", "").strip()
    if correlation:
        if not all(c.isalnum() or c in "._:-" for c in correlation) or len(correlation) > 64:
            raise AdapterValidationError("MCP_CORRELATION_ID is invalid")
        headers["X-Correlation-Id"] = correlation
    return headers


def _allowed_tools() -> set[str]:
    configured = os.getenv("MCP_ALLOWED_TOOLS")
    if configured is None:
        return set(_TOOL_NAMES)
    return {item.strip() for item in configured.split(",")} & _TOOL_NAMES


def _authorize(tool_name: str) -> None:
    role = os.getenv("MCP_ROLE", "employee").strip().lower()
    if tool_name not in _allowed_tools() or tool_name not in _ROLE_TOOLS.get(role, set()):
        raise PermissionError("tool is not enabled for the configured role")


def _validate_employee_id(employee_id: int) -> None:
    if isinstance(employee_id, bool) or not isinstance(employee_id, int):
        raise AdapterValidationError("employee_id must be an integer")
    if employee_id < 1 or employee_id > _MAX_EMPLOYEE_ID:
        raise AdapterValidationError("employee_id must be between 1 and 2147483647")


def _validate_question(question: str) -> None:
    if not isinstance(question, str):
        raise AdapterValidationError("question must be a string")
    if not question.strip():
        raise AdapterValidationError("question must not be empty")
    if len(question) > _MAX_QUESTION_LENGTH:
        raise AdapterValidationError("question exceeds the maximum length")
    if any(ord(char) < 32 and char not in "\n\r\t" for char in question):
        raise AdapterValidationError("question contains invalid control characters")


def _validate_search(value: str | None, name: str) -> None:
    if value is not None and (not isinstance(value, str) or len(value) > 200 or any(ord(c) < 32 for c in value)):
        raise AdapterValidationError(f"{name} is invalid")


def _request(method: str, path: str, *, timeout_name: str, **kwargs: Any) -> str:
    try:
        response = requests.request(
            method,
            f"{_base_url()}{path}",
            headers=_headers(),
            timeout=_timeout(timeout_name),
            allow_redirects=False,
            **kwargs,
        )
        if not 200 <= response.status_code < 300:
            return _error("upstream service request failed")
        return response.text
    except AdapterValidationError as exc:
        return _error(str(exc))
    except requests.Timeout:
        return _error("upstream service timed out")
    except (requests.RequestException, ValueError):
        # Do not include response text, URLs, exception details, or auth data.
        return _error("upstream service request failed")


def _guard(tool_name: str, operation: Callable[[], str]) -> str:
    try:
        _authorize(tool_name)
        return operation()
    except PermissionError as exc:
        return _error(str(exc))
    except AdapterValidationError as exc:
        return _error(str(exc))


@mcp.tool()
def get_employee(employee_id: int) -> str:
    """Get employee information using the employee ID."""
    return _guard(
        "get_employee",
        lambda: (_validate_employee_id(employee_id), _request("GET", f"/api/employees/{employee_id}", timeout_name="employee"))[1],
    )


@mcp.tool()
def assess_attrition_risk(employee_id: int) -> str:
    """Assess employee attrition risk using the machine learning model."""
    return _guard(
        "assess_attrition_risk",
        lambda: (_validate_employee_id(employee_id), _request("GET", f"/api/ai/employees/{employee_id}/risk", timeout_name="risk"))[1],
    )


@mcp.tool()
def search_hr_policy(question: str) -> str:
    """Search HR policies using the RAG system."""
    return _guard(
        "search_hr_policy",
        lambda: (_validate_question(question), _request("POST", "/api/rag/ask", timeout_name="policy", json={"question": question}))[1],
    )


@mcp.tool()
def get_department_statistics() -> str:
    """Get employee workforce statistics grouped by department."""
    return _guard(
        "get_department_statistics",
        lambda: _request("GET", "/api/dashboard/summary", timeout_name="statistics"),
    )


@mcp.tool()
def search_employees(query: str | None = None, department: str | None = None) -> str:
    """Search employees using the existing paged employee search endpoint."""
    return _guard("search_employees", lambda: (_validate_search(query, "query"), _validate_search(department, "department"),
        _request("GET", "/api/employees/search", timeout_name="search", params={k: v for k, v in {"query": query, "department": department}.items() if v is not None}))[2])


@mcp.tool()
def get_workforce_metrics() -> str:
    """Get workforce metrics from the existing dashboard summary endpoint."""
    return _guard("get_workforce_metrics", lambda: _request("GET", "/api/dashboard/summary", timeout_name="statistics"))


@mcp.tool()
def list_reports(size: int = 50) -> str:
    """List generated reports using the existing report endpoint."""
    if isinstance(size, bool) or not isinstance(size, int) or size < 1 or size > 100:
        return _error("size must be between 1 and 100")
    return _guard("list_reports", lambda: _request("GET", "/api/reports", timeout_name="reports", params={"size": size}))


if __name__ == "__main__":
    asyncio.run(mcp.run_streamable_http_async(host="127.0.0.1", port=8001))
