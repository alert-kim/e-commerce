package kr.hhplus.be.server.domain.balance

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigDecimal
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.domain.balance.exception.BelowMinBalanceAmountException
import kr.hhplus.be.server.domain.balance.exception.ExceedMaxBalanceAmountException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.math.RoundingMode

@DisplayName("잔고 금액 테스트")
class BalanceAmountTest {

    @Nested
    @DisplayName("금액 생성")
    inner class Create {
        @Test
        @DisplayName("최소값으로 생성 가능")
        fun minAmount() {
            val amount = BalanceAmount.of(BalanceAmount.MIN_AMOUNT)

            assertThat(amount.value).isEqualByComparingTo(BalanceAmount.MIN_AMOUNT)
        }

        @Test
        @DisplayName("최소값과 최대값 사이 생성 가능")
        fun betweenMinAndMax() {
            val value = Arb.bigDecimal(min = BalanceAmount.MIN_AMOUNT.plus(BigDecimal.ONE), max = BalanceAmount.MAX_AMOUNT.minus(BigDecimal.ONE)).next()
            val amount = BalanceAmount.of(value)

            assertThat(amount.value).isEqualByComparingTo(value.setScale(2, RoundingMode.HALF_UP))
        }

        @Test
        @DisplayName("최대값으로 생성 가능")
        fun maxAmount() {
            val amount = BalanceAmount.of(BalanceAmount.MAX_AMOUNT)

            assertThat(amount.value).isEqualByComparingTo(BalanceAmount.MAX_AMOUNT)
        }

        @Test
        @DisplayName("최소값 미만 생성 불가")
        fun belowMinAmount() {
            assertThrows<BelowMinBalanceAmountException> {
                BalanceAmount.of(BalanceAmount.MIN_AMOUNT.subtract(BigDecimal.ONE))
            }
        }

        @Test
        @DisplayName("최대값 초과 생성 불가")
        fun exceedMaxAmount() {
            assertThrows<ExceedMaxBalanceAmountException> {
                BalanceAmount.of(BalanceAmount.MAX_AMOUNT.add(BigDecimal.ONE))
            }
        }
    }

    @Nested
    @DisplayName("연산")
    inner class Operation {
        @Test
        @DisplayName("더하기 연산은 두 값의 합 반환")
        fun plus() {
            val amountValue1 = BigDecimal.valueOf(10_000)
            val amountValue2 = BigDecimal.valueOf(30_000)
            val sum = amountValue1.add(amountValue2)

            val result = BalanceAmount.of(amountValue1) + BalanceAmount.of(amountValue2)

            assertThat(result.value).isEqualByComparingTo(sum)
        }

        @Test
        @DisplayName("더하기 결과가 최대값 초과 시 예외 발생")
        fun plusExceedMax() {
            val amount1 = BalanceAmount.of(BalanceAmount.MAX_AMOUNT)
            val amount2 = BalanceAmount.of(BigDecimal.ONE)

            assertThrows<ExceedMaxBalanceAmountException> {
                amount1 + amount2
            }
        }

        @Test
        @DisplayName("빼기 연산은 두 값의 차 반환")
        fun minus() {
            val amountValue1 = BigDecimal.valueOf(30_000)
            val amountValue2 = BigDecimal.valueOf(10_000)
            val minus = amountValue1.minus(amountValue2)

            val result = BalanceAmount.of(amountValue1) - BalanceAmount.of(amountValue2)

            assertThat(result.value).isEqualByComparingTo(minus)
        }

        @Test
        @DisplayName("빼기 결과가 최소값 미만 시 예외 발생")
        fun minusBelowMin() {
            val amount1 = BalanceAmount.of(BalanceAmount.MIN_AMOUNT)
            val amount2 = BalanceAmount.of(BigDecimal.ONE)

            assertThrows<BelowMinBalanceAmountException> {
                amount1 - amount2
            }
        }
    }
}
