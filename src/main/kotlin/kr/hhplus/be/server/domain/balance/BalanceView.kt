package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

data class BalanceView(
    val id: BalanceId,
    val userId: UserId,
    val amount: BigDecimal,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(
            balance: Balance,
        ) = BalanceView(
            id = balance.requireId(),
            userId = balance.userId,
            amount = balance.amount,
            createdAt = balance.createdAt,
            updatedAt = balance.updatedAt,
        )
    }
}
