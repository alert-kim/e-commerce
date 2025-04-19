package kr.hhplus.be.server.interfaces.balance.response

import kr.hhplus.be.server.domain.balance.BalanceView
import kr.hhplus.be.server.domain.user.UserId
import kr.hhplus.be.server.interfaces.common.ServerApiResponse
import java.math.BigDecimal
import java.time.Instant

data class BalanceResponse(
    val userId: Long,
    val amount: BigDecimal,
    val createdAt: Instant,
    val updatedAt: Instant,
): ServerApiResponse {
    companion object {
        fun of(
            userId: UserId,
            balance: BalanceView?,
        ) = when (balance) {
            null -> default(userId)
            else -> BalanceResponse(
                userId = balance.userId.value,
                amount = balance.amount,
                createdAt = balance.createdAt,
                updatedAt = balance.updatedAt,
            )
        }

        private fun default(userId: UserId) = BalanceResponse(
            userId = userId.value,
            amount = BigDecimal.ZERO,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
    }
}
