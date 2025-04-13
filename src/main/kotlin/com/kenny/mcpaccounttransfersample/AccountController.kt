package com.kenny.mcpaccounttransfersample

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/accounts")
class AccountController {
    private val accountData = mutableMapOf("kennywithbella" to 100_000)

    @GetMapping("/{id}/balance")
    fun getBalance(@PathVariable id: String): ResponseEntity<Int> {
        val balance = accountData[id] ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/transfer")
    fun transfer( @RequestBody request: TransferRequest): ResponseEntity<String> {
        val fromBalance = accountData[request.from] ?: return ResponseEntity.notFound().build()
        if( fromBalance < request.amount ) return ResponseEntity.badRequest().body("Insufficient funds")
        accountData[request.from] = fromBalance - request.amount
        println("Transferred ${request.amount} from ${request.from}")
        return ResponseEntity.ok("Transferred ${request.amount} from ${request.from}")
    }
}