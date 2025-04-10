package kr.hhplus.be.server.domain.balance

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigDecimal
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.domain.balance.exception.BelowMinBalanceException
import kr.hhplus.be.server.domain.balance.exception.ExceedMaxBalanceException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class BalanceAmountTest {

    @Test
    fun `BalanceAmount는 최소값이 될 수 있다`() {
        val amount = BalanceAmount(BalanceAmount.MIN_AMOUNT)

        assertThat(amount.value).isEqualByComparingTo(BalanceAmount.MIN_AMOUNT)
    }

    @Test
    fun `BalanceAmount는 최소값 부터 최대값 사이의 값을 가질 수 있다`() {
        val value = Arb.bigDecimal(min = BalanceAmount.MIN_AMOUNT.plus(BigDecimal.ONE), max = BalanceAmount.MAX_AMOUNT.minus(BigDecimal.ONE)).next()
        val amount = BalanceAmount(value)

        assertThat(amount.value).isEqualByComparingTo(value)
    }

    @Test
    fun `BalanceAmount는 최대값이 될 수 있다`() {
        val amount = BalanceAmount(BalanceAmount.MAX_AMOUNT)

        assertThat(amount.value).isEqualByComparingTo(BalanceAmount.MAX_AMOUNT)
    }

    @Test
    fun `BalanceAmount는 최소값 미만일 수 없다`() {
        assertThrows<BelowMinBalanceException> {
            BalanceAmount(BalanceAmount.MIN_AMOUNT.subtract(BigDecimal.ONE))
        }
    }

    @Test
    fun `BalanceAmount는 최대값을 초과할 수 없다`() {
        assertThrows<ExceedMaxBalanceException> {
            BalanceAmount(BalanceAmount.MAX_AMOUNT.add(BigDecimal.ONE))
        }
    }

    @Test
    fun `BalanceAmount의 plus 연산자는 두 값을 더한 결과를 반환한다`() {
        val amountValue1 = BigDecimal.valueOf(10_000)
        val amountValue2 = BigDecimal.valueOf(30_000)
        val sum = amountValue1.add(amountValue2)

        val result = BalanceAmount(amountValue1) + BalanceAmount(amountValue2)

        assertThat(result.value).isEqualByComparingTo(sum)
    }


    @Test
    fun `plus 연산 결과가 최대값을 초과하면 예외를 던진다`() {
        val amount1 = BalanceAmount(BalanceAmount.MAX_AMOUNT)
        val amount2 = BalanceAmount(BigDecimal.ONE)

        assertThrows<ExceedMaxBalanceException> {
            amount1 + amount2
        }
    }
}
