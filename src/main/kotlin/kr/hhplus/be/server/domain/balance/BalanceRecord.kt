package kr.hhplus.be.server.domain.balance

import java.time.Instant

class BalanceRecord(
    val id: BalanceRecordId? = null,
    val balanceId: BalanceId,
    val type: BalanceTransactionType,
    val amount: BalanceAmount,
    val createdAt: Instant
) {
    companion object {
        fun new(
            balanceId: BalanceId,
            type: BalanceTransactionType,
            amount: BalanceAmount,
        ): BalanceRecord =
            BalanceRecord(
                balanceId = balanceId,
                type = type,
                amount = amount,
                createdAt = Instant.now()
            )
    }
}
