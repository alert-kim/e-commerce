package kr.hhplus.be.server.domain.balance.dto

import kr.hhplus.be.server.domain.balance.Balance
import kr.hhplus.be.server.domain.balance.BalanceId
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

data class BalanceQueryModel(
    val id: BalanceId,
    val userId: UserId,
    val amount: BigDecimal,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(
            balance: Balance,
        ) = BalanceQueryModel(
            id = balance.requireId(),
            userId = balance.userId,
            amount = balance.amount,
            createdAt = balance.createdAt,
            updatedAt = balance.updatedAt,
        )
    }
}
