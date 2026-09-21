import requests
from mcp.server import MCPServer
import asyncio

mcp = MCPServer("Employee Intelligence MCP")

SPRING_BOOT_URL = "http://localhost:8080"


@mcp.tool()
def get_employee(employee_id: int) -> str:
    """
    Get employee information using the employee ID.
    """
    response = requests.get(
        f"{SPRING_BOOT_URL}/api/employees/{employee_id}",
        timeout=10
    )

    response.raise_for_status()

    return response.text


@mcp.tool()
def assess_attrition_risk(employee_id: int) -> str:
    """
    Assess employee attrition risk using the machine learning model.
    """
    response = requests.get(
        f"{SPRING_BOOT_URL}/api/ai/employees/{employee_id}/risk",
        timeout=30
    )

    response.raise_for_status()

    return response.text


@mcp.tool()
def search_hr_policy(question: str) -> str:
    """
    Search HR policies using the RAG system.
    """
    response = requests.post(
        f"{SPRING_BOOT_URL}/api/rag/ask",
        json={"question": question},
        timeout=60
    )

    response.raise_for_status()

    return response.text


@mcp.tool()
def get_department_statistics() -> str:
    """
    Get employee workforce statistics grouped by department.
    """
    response = requests.get(
        f"{SPRING_BOOT_URL}/api/dashboard/summary",
        timeout=10
    )

    response.raise_for_status()

    return response.text
if __name__ == "__main__":
    asyncio.run(
        mcp.run_streamable_http_async(
            host="127.0.0.1",
            port=8001
        )
    )