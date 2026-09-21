import asyncio

from mcp import ClientSession
from mcp.client.streamable_http import streamable_http_client


async def main():
    async with streamable_http_client(
        "http://127.0.0.1:8001/mcp"
    ) as (read_stream, write_stream):

        async with ClientSession(
            read_stream,
            write_stream
        ) as session:

            await session.initialize()

            tools = await session.list_tools()

            print("MCP HTTP tools:")

            for tool in tools.tools:
                print(f"- {tool.name}")

            result = await session.call_tool(
                "get_employee",
                {"employee_id": 105}
            )

            print("\nget_employee result:")
            print(result.content)


if __name__ == "__main__":
    asyncio.run(main())