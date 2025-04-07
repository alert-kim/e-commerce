package kr.hhplus.be.server.controller.balance.response

import java.math.BigDecimal
import java.time.Instant

class UserBalanceResponse(
    val id: Long,
    val userId: Long,
    val balance: BigDecimal,
    val createdAt: Instant,
    val updatedAt: Instant,
)
