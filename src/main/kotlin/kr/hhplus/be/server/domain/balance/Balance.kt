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

    var amount: BigDecimal = _amount.value
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun charge(amount: BalanceAmount) {
        TODO()
    }

    fun requireId(): BalanceId =
        id ?: throw RequiredBalanceIdException()

    companion object {
        fun new(userId: UserId): Balance =
            TODO()
    }
}
