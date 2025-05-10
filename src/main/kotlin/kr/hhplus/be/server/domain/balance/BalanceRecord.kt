package kr.hhplus.be.server.domain.balance

import jakarta.persistence.AttributeOverride
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import kr.hhplus.be.server.domain.balance.exception.RequiredBalanceIdException
import java.time.Instant

@Entity
@Table(
    name = "balance_records",
    indexes = [
        Index(name = "balance_record_idx_balance", columnList = "balance_id"),
    ]
)
class BalanceRecord(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null,
    @Column(nullable = false)
    val balanceId: BalanceId,
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)")
    val type: BalanceTransactionType,
    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "amount"))
    @Column(nullable = false)
    val amount: BalanceAmount,
    @Column(nullable = false)
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
