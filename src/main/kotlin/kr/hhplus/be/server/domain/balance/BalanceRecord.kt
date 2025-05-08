package kr.hhplus.be.server.domain.balance

import jakarta.persistence.*
import kr.hhplus.be.server.domain.balance.exception.RequiredBalanceIdException
import java.time.Instant

@Entity
@Table(name = "balance_records")
class BalanceRecord(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null,
    val balanceId: BalanceId,
    val type: BalanceTransactionType,
    val amount: BalanceAmount,
    val createdAt: Instant
) {
    fun id(): BalanceRecordId =
        id?.let { BalanceRecordId(it) } ?: throw RequiredBalanceIdException()

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
