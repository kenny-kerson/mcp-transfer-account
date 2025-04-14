# 💸 실습: AI 에이전트 → MCP 서버 → 계좌이체 서버

## 🔁 전체 구성 흐름

```text
[AI 에이전트]
   |
   | 자연어: "kennywithbella 계좌에서 1만원 이체해줘"
   v
[MCP 서버 (Python)]
   |
   | MCP Tool: POST /transfer, Resource: GET /balance
   v
[계좌이체 API 서버 (Spring Boot + Kotlin)]
```

---

## 🧩 Step 1: 계좌이체 API 서버 (Spring Boot + Kotlin)

```kotlin
@RestController
@RequestMapping("/accounts")
class AccountController {
    private val accountData = mutableMapOf("kennywithbella" to 100_000)

    @GetMapping("/{id}/balance")
    fun getBalance(@PathVariable id: String): ResponseEntity<Int> {
        val balance = accountData[id] ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(balance)
    }

    @PostMapping("/transfer")
    fun transfer(@RequestBody request: TransferRequest): ResponseEntity<String> {
        val fromBalance = accountData[request.from] ?: return ResponseEntity.notFound().build()
        if (fromBalance < request.amount) return ResponseEntity.badRequest().body("Insufficient funds")
        accountData[request.from] = fromBalance - request.amount
        println("Transferred ${request.amount} from ${request.from}")
        return ResponseEntity.ok("Transferred ${request.amount} from ${request.from}")
    }
}
```

> 🔗 전체 코드는 [계좌이체 API 서버 및 MCP 서버 샘플코드](https://github.com/kenny-kerson/mcp-transfer-account)에서 확인할 수 있습니다.

---

## ⚙️ Step 2: MCP 서버 설정

### 🔸 uv 설치

```bash
curl -LsSf https://astral.sh/uv/install.sh | sh
```

> 💡 *uv는 기존 pip을 대체하는 Python 패키지 관리 도구입니다.*

### 🔸 fastmcp 설치

```bash
uv venv                    # Python 가상환경 셋팅
uv pip install fastmcp     # fastmcp 설치
```

> ⚠️ `uv venv`는 기존 가상환경이 없을 경우에만 실행하세요.

### 🔸 fastmcp 실행 테스트

```bash
./.venv/bin/fastmcp dev fastmcp_server.py
```

### 🔸 MCP 서버 코드 (Python)

```python
from fastmcp import FastMCP
import requests

mcp = FastMCP("계좌이체 MCP 서버")

@mcp.resource("balance://{account_id}")
def get_balance(account_id: str) -> dict:
    response = requests.get(f"http://localhost:8080/accounts/{account_id}/balance")
    if response.status_code == 200:
        return {"balance": response.json()}
    else:
        return {"error": "잔액 조회 실패 또는 계좌 없음"}

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

if __name__ == "__main__":
    mcp.run()
```

---

## 🧠 Step 3: AI 에이전트 연동 (Claude)

### 📥 Claude Desktop 설치

1. [Claude 다운로드 페이지](https://claude.ai/download) 접속
2. 운영체제(macOS 또는 Windows) 선택 후 설치 파일 다운로드
3. 설치 및 실행
4. Claude 계정으로 로그인

### ⚙️ MCP 서버 설정

1. Claude Desktop 실행 후 **Settings > Developer > Edit Config** 선택
2. `claude_desktop_config.json` 파일에 아래 항목 추가:

```json
{
  "mcpServers": {
    "계좌이체 MCP 서버": {
      "command": "/Users/kenny/kenny-source/Spring/mcp-account-transfer-sample/.venv/bin/python",
      "args": [
        "/Users/kenny/kenny-source/Spring/mcp-account-transfer-sample/fastmcp_server.py"
      ]
    }
  }
}
```

3. Claude 재시작
4. 하단 망치 아이콘이 나타나면 MCP 서버 연결 성공

---

## 🧪 Step 4: 테스트

1. Claude 프롬프트에 아래 문장 입력:
   ```
   kennywithbella 계좌에서 1만원 이체해줘
   ```
2. 계좌이체 API 서버 콘솔에 다음과 같은 로그 확인:
   ```
   Transferred 10000 from kennywithbella
   ```

---

## 📚 참고자료

- 🎥 [요즘 난리난 MCP! 10분안에 정리해줌](https://youtu.be/EswVjHZMn74?si=UrdobIo8qcylVbIC)
- 🛠 [fastMCP V2 Github Repository](https://github.com/jlowin/fastmcp?tab=readme-ov-file#tools)
