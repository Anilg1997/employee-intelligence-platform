import asyncio

async def main():
    from mcp import ClientSession, StdioServerParameters
    from mcp.client.stdio import stdio_client

    server_params = StdioServerParameters(
        command="python",
        args=["server.py"],
    )

    async with stdio_client(server_params) as (read, write):
        async with ClientSession(read, write) as session:

            await session.initialize()

            result = await session.call_tool(
                "get_department_statistics",
                {}
            )

            print("get_department_statistics result:")
            print(result.content)


if __name__ == "__main__":
    asyncio.run(main())
