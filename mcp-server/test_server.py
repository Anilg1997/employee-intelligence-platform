import importlib
import os
import sys
from types import ModuleType
from unittest.mock import Mock, patch

import pytest


@pytest.fixture
def server(monkeypatch):
    for name in list(os.environ):
        if name.startswith(("MCP_", "SPRING_API_")):
            monkeypatch.delenv(name)
    monkeypatch.setenv("MCP_ROLE", "manager")
    monkeypatch.setenv("SPRING_API_BASE_URL", "https://spring.example.test/")
    monkeypatch.setenv("SPRING_API_BEARER_TOKEN", "test-secret")
    # Unit tests exercise the adapter independently of the existing MCP runtime.
    # This no-op decorator deliberately does not claim transport validation.
    mcp_module = ModuleType("mcp.server")
    mcp_module.MCPServer = Mock(return_value=Mock(tool=lambda: lambda func: func))
    mcp_package = ModuleType("mcp")
    mcp_package.server = mcp_module
    monkeypatch.setitem(sys.modules, "mcp", mcp_package)
    monkeypatch.setitem(sys.modules, "mcp.server", mcp_module)
    monkeypatch.syspath_prepend(os.path.dirname(__file__))
    import server as module

    return importlib.reload(module)


def response(body="{\"ok\":true}", status=200):
    value = Mock()
    value.text = body
    value.status_code = status
    value.raise_for_status.side_effect = None if status < 400 else __import__("requests").HTTPError("upstream body SECRET")
    return value


def test_forwards_bearer_and_uses_allowlisted_endpoint(server):
    with patch.object(server.requests, "request", return_value=response()) as request:
        assert server.get_employee(42) == '{"ok":true}'

    request.assert_called_once_with(
        "GET",
        "https://spring.example.test/api/employees/42",
        headers={"Authorization": "Bearer test-secret"},
        timeout=10.0,
        allow_redirects=False,
    )


def test_policy_request_and_bounded_configured_timeout(server, monkeypatch):
    monkeypatch.setenv("MCP_POLICY_TIMEOUT_SECONDS", "900")
    with patch.object(server.requests, "request", return_value=response()) as request:
        assert server.search_hr_policy("Where is leave policy?") == '{"ok":true}'
    assert request.call_args.kwargs["timeout"] == 30.0
    assert request.call_args.kwargs["json"] == {"question": "Where is leave policy?"}


@pytest.mark.parametrize("value", [0, -1, True, "42"])
def test_rejects_invalid_employee_id_without_request(server, value):
    with patch.object(server.requests, "request") as request:
        result = server.get_employee(value)
    assert result.startswith("Error: employee_id")
    request.assert_not_called()


def test_rejects_invalid_question_without_request(server):
    with patch.object(server.requests, "request") as request:
        result = server.search_hr_policy(" ")
    assert result == "Error: question must not be empty"
    request.assert_not_called()


def test_role_boundary_blocks_risk(server, monkeypatch):
    monkeypatch.setenv("MCP_ROLE", "employee")
    with patch.object(server.requests, "request") as request:
        result = server.assess_attrition_risk(42)
    assert result == "Error: tool is not enabled for the configured role"
    request.assert_not_called()


def test_upstream_error_does_not_leak_body_or_secret(server):
    with patch.object(server.requests, "request", return_value=response("SECRET upstream payload", 500)):
        result = server.get_department_statistics()
    assert result == "Error: upstream service request failed"
    assert "SECRET" not in result


def test_invalid_base_url_is_safe_error(server, monkeypatch):
    monkeypatch.setenv("SPRING_API_BASE_URL", "file:///etc/passwd")
    with patch.object(server.requests, "request") as request:
        result = server.get_employee(42)
    assert result.startswith("Error: SPRING_API_BASE_URL")
    request.assert_not_called()


@pytest.mark.parametrize("tool,path,args", [
    ("assess_attrition_risk", "/api/ai/employees/42/risk", (42,)),
    ("get_department_statistics", "/api/dashboard/summary", ()),
])
def test_other_tool_routes(server, tool, path, args):
    with patch.object(server.requests, "request", return_value=response()) as request:
        assert getattr(server, tool)(*args) == '{"ok":true}'
    assert request.call_args.args == ("GET", "https://spring.example.test" + path)


@pytest.mark.parametrize("role,allowlist", [
    ("unknown", "get_employee"), ("manager", ""),
    ("manager", "search_hr_policy"), ("manager", "not_a_tool"),
    ("employee", "get_employee"),
])
def test_policy_fails_closed(server, monkeypatch, role, allowlist):
    monkeypatch.setenv("MCP_ROLE", role)
    monkeypatch.setenv("MCP_ALLOWED_TOOLS", allowlist)
    with patch.object(server.requests, "request") as request:
        assert server.get_employee(1).startswith("Error: tool is not enabled")
    request.assert_not_called()


@pytest.mark.parametrize("question", [None, 12, "a" * 2001, "bad\x00input"])
def test_invalid_policy_input(server, question):
    with patch.object(server.requests, "request") as request:
        assert server.search_hr_policy(question).startswith("Error: question")
    request.assert_not_called()


@pytest.mark.parametrize("employee_id", [1.5, 2147483648, None, "../secrets"])
def test_risk_id_validation(server, employee_id):
    with patch.object(server.requests, "request") as request:
        assert server.assess_attrition_risk(employee_id).startswith("Error: employee_id")
    request.assert_not_called()


@pytest.mark.parametrize("timeout,expected", [
    ("nan", 10), ("inf", 10), ("invalid", 10), ("-10", 0.1), ("0", 0.1), ("5", 5),
])
def test_timeout_configuration_is_bounded(server, monkeypatch, timeout, expected):
    monkeypatch.setenv("MCP_EMPLOYEE_TIMEOUT_SECONDS", timeout)
    with patch.object(server.requests, "request", return_value=response()) as request:
        server.get_employee(1)
    assert request.call_args.kwargs["timeout"] == expected


@pytest.mark.parametrize("status", [301, 302, 401, 403, 404, 429, 503])
def test_redirect_and_http_errors_are_sanitized(server, status):
    with patch.object(server.requests, "request", return_value=response("SECRET", status)) as request:
        assert server.get_employee(1) == "Error: upstream service request failed"
    assert request.call_args.kwargs["allow_redirects"] is False


def test_network_timeout_is_sanitized(server):
    with patch.object(server.requests, "request", side_effect=server.requests.Timeout("test-secret")):
        assert server.get_employee(1) == "Error: upstream service timed out"


def test_connection_error_is_sanitized(server):
    with patch.object(server.requests, "request", side_effect=server.requests.ConnectionError("test-secret")):
        assert server.get_employee(1) == "Error: upstream service request failed"


@pytest.mark.parametrize("url", [
    "https://user:SECRET@example.test", "https://example.test?token=SECRET",
    "https://example.test#SECRET", "https://example.test:SECRET", "https://[SECRET",
])
def test_bad_url_configuration_does_not_leak(server, monkeypatch, url):
    monkeypatch.setenv("SPRING_API_BASE_URL", url)
    with patch.object(server.requests, "request") as request:
        assert server.get_employee(1) == "Error: SPRING_API_BASE_URL is invalid"
    request.assert_not_called()


def test_token_header_injection_rejected(server, monkeypatch):
    monkeypatch.setenv("SPRING_API_BEARER_TOKEN", "secret\r\nX-Injected: yes")
    with patch.object(server.requests, "request") as request:
        assert server.get_employee(1) == "Error: SPRING_API_BEARER_TOKEN is invalid"
    request.assert_not_called()


def test_missing_token_omits_authorization(server, monkeypatch):
    monkeypatch.delenv("SPRING_API_BEARER_TOKEN")
    with patch.object(server.requests, "request", return_value=response()) as request:
        server.get_employee(1)
    assert request.call_args.kwargs["headers"] == {}


def test_new_read_tools_use_existing_endpoints(server):
    with patch.object(server.requests, "request", return_value=response()) as request:
        assert server.search_employees("ana", "Engineering") == '{"ok":true}'
        assert request.call_args.kwargs["params"] == {"query": "ana", "department": "Engineering"}
        assert server.get_workforce_metrics() == '{"ok":true}'
        assert request.call_args.args[1].endswith("/api/dashboard/summary")
        assert server.list_reports(10) == '{"ok":true}'
        assert request.call_args.kwargs["params"] == {"size": 10}


def test_correlation_id_is_propagated_and_validated(server, monkeypatch):
    monkeypatch.setenv("MCP_CORRELATION_ID", "agent-123")
    with patch.object(server.requests, "request", return_value=response()) as request:
        server.get_employee(1)
    assert request.call_args.kwargs["headers"]["X-Correlation-Id"] == "agent-123"
    monkeypatch.setenv("MCP_CORRELATION_ID", "bad\r\nheader")
    with patch.object(server.requests, "request") as request:
        assert server.get_employee(1) == "Error: MCP_CORRELATION_ID is invalid"
    request.assert_not_called()
