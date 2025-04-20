package kr.hhplus.be.server.domain.balance

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigDecimal
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.domain.balance.exception.BelowMinBalanceAmountException
import kr.hhplus.be.server.domain.balance.exception.ExceedMaxBalanceAmountException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.math.RoundingMode

class BalanceAmountTest {

    @Test
    fun `BalanceAmount는 최소값이 될 수 있다`() {
        val amount = BalanceAmount.of(BalanceAmount.MIN_AMOUNT)

        assertThat(amount.value).isEqualByComparingTo(BalanceAmount.MIN_AMOUNT)
    }

    @Test
    fun `BalanceAmount는 최소값 부터 최대값 사이의 값을 가질 수 있다`() {
        val value = Arb.bigDecimal(min = BalanceAmount.MIN_AMOUNT.plus(BigDecimal.ONE), max = BalanceAmount.MAX_AMOUNT.minus(BigDecimal.ONE)).next()
        val amount = BalanceAmount.of(value)

        assertThat(amount.value).isEqualByComparingTo(value.setScale(2, RoundingMode.HALF_UP))
    }

    @Test
    fun `BalanceAmount는 최대값이 될 수 있다`() {
        val amount = BalanceAmount.of(BalanceAmount.MAX_AMOUNT)

        assertThat(amount.value).isEqualByComparingTo(BalanceAmount.MAX_AMOUNT)
    }

    @Test
    fun `BalanceAmount는 최소값 미만일 수 없다`() {
        assertThrows<BelowMinBalanceAmountException> {
            BalanceAmount.of(BalanceAmount.MIN_AMOUNT.subtract(BigDecimal.ONE))
        }
    }

    @Test
    fun `BalanceAmount는 최대값을 초과할 수 없다`() {
        assertThrows<ExceedMaxBalanceAmountException> {
            BalanceAmount.of(BalanceAmount.MAX_AMOUNT.add(BigDecimal.ONE))
        }
    }

    @Test
    fun `BalanceAmount의 plus 연산자는 두 값을 더한 결과를 반환한다`() {
        val amountValue1 = BigDecimal.valueOf(10_000)
        val amountValue2 = BigDecimal.valueOf(30_000)
        val sum = amountValue1.add(amountValue2)

        val result = BalanceAmount.of(amountValue1) + BalanceAmount.of(amountValue2)

        assertThat(result.value).isEqualByComparingTo(sum)
    }


    @Test
    fun `plus 연산 결과가 최대값을 초과하면 예외를 던진다`() {
        val amount1 = BalanceAmount.of(BalanceAmount.MAX_AMOUNT)
        val amount2 = BalanceAmount.of(BigDecimal.ONE)

        assertThrows<ExceedMaxBalanceAmountException> {
            amount1 + amount2
        }
    }

    @Test
    fun `BalanceAmount의 minus 연산자는 두 값을 뺀 결과를 반환한다`() {
        val amountValue1 = BigDecimal.valueOf(30_000)
        val amountValue2 = BigDecimal.valueOf(10_000)
        val minus = amountValue1.minus(amountValue2)

        val result = BalanceAmount.of(amountValue1) - BalanceAmount.of(amountValue2)

        assertThat(result.value).isEqualByComparingTo(minus)
    }


    @Test
    fun `minus 연산 결과가 최소값을 초과하면 예외를 던진다`() {
        val amount1 = BalanceAmount.of(BalanceAmount.MIN_AMOUNT)
        val amount2 = BalanceAmount.of(BigDecimal.ONE)

        assertThrows<BelowMinBalanceAmountException> {
            amount1 - amount2
        }
    }
}
