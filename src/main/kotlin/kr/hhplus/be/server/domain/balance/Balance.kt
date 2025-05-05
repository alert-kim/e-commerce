package kr.hhplus.be.server.domain.balance

import jakarta.persistence.*
import kr.hhplus.be.server.domain.balance.exception.RequiredBalanceIdException
import kr.hhplus.be.server.domain.balance.result.UsedBalanceAmount
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "balances",
    uniqueConstraints = [
        UniqueConstraint(name = "balance_unq_user", columnNames = ["user_id"]),
    ]
)
class Balance (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null,
    val userId: UserId,
    val createdAt: Instant,
    amount: BalanceAmount,
    updatedAt: Instant,
) {

    var amount: BalanceAmount = amount
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun charge(amount: BalanceAmount) {
        this.amount = this.amount.plus(amount)
        this.updatedAt = Instant.now()
    }

    fun use(amount: BalanceAmount): UsedBalanceAmount {
        this.amount = this.amount.minus(amount)
        this.updatedAt = Instant.now()
        return UsedBalanceAmount(
            balanceId = id(),
            amount = amount,
        )
    }
    
    fun cancelUse(amount: BalanceAmount) {
        this.amount = this.amount.plus(amount)
        this.updatedAt = Instant.now()
    }

    fun id(): BalanceId =
        id?.let { BalanceId(id) } ?: throw RequiredBalanceIdException()

    companion object {
        fun new(userId: UserId): Balance =
            Balance(
                userId = userId,
                amount = BalanceAmount.of(BigDecimal.ZERO),
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )
    }
}
