package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.balance.exception.RequiredBalanceIdException
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

class Balance (
    val id: BalanceId? = null,
    val userId: UserId,
    val createdAt: Instant,
    amount: BigDecimal,
    updatedAt: Instant,
) {
    private var _amount: BalanceAmount = BalanceAmount(amount)

    var amount: BigDecimal
        get() = _amount.value
        private set(value) {
            this._amount = BalanceAmount(value)
        }

    var updatedAt: Instant = updatedAt
        private set

    fun charge(amount: BalanceAmount) {
        this._amount = this._amount.plus(amount)
        this.updatedAt = Instant.now()
    }

    fun requireId(): BalanceId =
        id ?: throw RequiredBalanceIdException()

    companion object {
        fun new(userId: UserId): Balance =
            Balance(
                userId = userId,
                createdAt = Instant.now(),
                amount = BigDecimal.ZERO,
                updatedAt = Instant.now(),
            )
    }
}
