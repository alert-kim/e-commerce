package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

class BalanceQueryModel (
    val id: BalanceId,
    val userId: UserId,
    val amount: BigDecimal,
    val createdAt: Instant,
    val updatedAt: Instant,
)
