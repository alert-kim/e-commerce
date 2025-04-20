package kr.hhplus.be.server.domain.balance

import jakarta.persistence.Embeddable
import kr.hhplus.be.server.domain.balance.exception.BelowMinBalanceAmountException
import kr.hhplus.be.server.domain.balance.exception.ExceedMaxBalanceAmountException
import java.math.BigDecimal
import java.math.RoundingMode

@Embeddable
data class BalanceAmount private constructor(val value: BigDecimal) {
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

    operator fun minus(amount: BalanceAmount): BalanceAmount =
        BalanceAmount(this.value.minus(amount.value))

    companion object {
        val MIN_AMOUNT: BigDecimal = BigDecimal.valueOf(0)
        val MAX_AMOUNT: BigDecimal = BigDecimal.valueOf(1_000_000)

        fun of(value: BigDecimal): BalanceAmount =
            BalanceAmount(value.setScale(2, RoundingMode.HALF_UP))
    }
}
