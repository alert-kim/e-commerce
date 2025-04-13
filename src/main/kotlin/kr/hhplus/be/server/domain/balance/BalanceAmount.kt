package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.balance.exception.BelowMinBalanceAmountException
import kr.hhplus.be.server.domain.balance.exception.ExceedMaxBalanceAmountException
import java.math.BigDecimal

@JvmInline
value class BalanceAmount (val value: BigDecimal) {
    init {
        if (value < MIN_AMOUNT) {
            throw BelowMinBalanceAmountException(value)
        }
        if (value > MAX_AMOUNT) {
            throw ExceedMaxBalanceAmountException(value)
        }
    }

    operator fun plus(amount: BalanceAmount): BalanceAmount =
        BalanceAmount(this.value.plus(amount.value))

    companion object {
        val MIN_AMOUNT: BigDecimal = BigDecimal.valueOf(0)
        val MAX_AMOUNT: BigDecimal = BigDecimal.valueOf(1_000_000)
    }
}
