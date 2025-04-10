package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.balance.BalanceId
import kr.hhplus.be.server.domain.balance.BalanceRecordId
import kr.hhplus.be.server.domain.balance.BalanceTransactionType
import java.time.Instant

class BalanceRecord(
    val id: BalanceRecordId? = null,
    val balanceId: BalanceId,
    val type: BalanceTransactionType,
    val balance: BalanceAmount,
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
                balance = amount,
                createdAt = Instant.now()
            )
    }
}
