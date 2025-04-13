from mcp.server.fastmcp import FastMCP
# from mcp.server.decorators import tool, resource
import requests

# MCP 서버 초기화
mcp = FastMCP("계좌이체 MCP 서버")

# Resource: 계좌 잔액 조회
@mcp.resource("balance://{account_id}")
def get_balance(account_id: str) -> dict:
    response = requests.get(f"http://localhost:8080/accounts/{account_id}/balance")
    if response.status_code == 200:
        return {"balance": response.json()}
    else:
        return {"error": "잔액 조회 실패 또는 계좌 없음"}

# Tool: 계좌이체 실행
@mcp.tool()
def transfer_money(from_account: str, amount: int) -> dict:
    payload = {
        "from": from_account,
        "amount": amount
    }
    response = requests.post("http://localhost:8080/accounts/transfer", json=payload)
    if response.status_code == 200:
        return {"result": response.text}
    else:
        return {"error": response.text}

# MCP 서버 실행
if __name__ == "__main__":
    mcp.run()