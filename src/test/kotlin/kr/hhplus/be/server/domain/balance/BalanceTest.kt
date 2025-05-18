package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.balance.exception.RequiredBalanceIdException
import kr.hhplus.be.server.testutil.mock.BalanceMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class BalanceTest {

    @Nested
    @DisplayName("ID 조회")
    inner class Id {
        @Test
        @DisplayName("ID가 존재하면 반환")
        fun idExists() {
            val balance = BalanceMock.balance(id = BalanceMock.id())

            val result = balance.id()

            assertThat(result).isEqualTo(balance.id())
        }

        @Test
        @DisplayName("ID가 없으면 예외 발생")
        fun idNotExists() {
            val balance = BalanceMock.balance(id = null)

            assertThrows<RequiredBalanceIdException> {
                balance.id()
            }
        }
    }

    @Nested
    @DisplayName("생성")
    inner class Create {
        @Test
        @DisplayName("유저ID로 0원 잔고 생성")
        fun new() {
            val userId = UserMock.id()

            val balance = Balance.new(userId)

            assertAll(
                { assertThat(balance.userId).isEqualTo(userId) },
                { assertThat(balance.amount.value).isEqualByComparingTo(BigDecimal.ZERO) },
            )
        }
    }

    @Nested
    @DisplayName("충전")
    inner class Charge {
        @Test
        @DisplayName("금액을 충전하고 업데이트 시간 갱신")
        fun charge() {
            val initialAmount = BigDecimal.valueOf(1_000)
            val balance = BalanceMock.balance(amount = initialAmount)
            val chargeAmount = BigDecimal.valueOf(2_000)

            balance.charge(BalanceAmount.of(chargeAmount))

            assertThat(balance.amount.value).isEqualByComparingTo(initialAmount.add(chargeAmount))
            assertThat(balance.updatedAt).isAfter(balance.createdAt)
        }
    }

    @Nested
    @DisplayName("사용")
    inner class Use {
        @Test
        @DisplayName("금액을 사용하고 업데이트 시간 갱신")
        fun use() {
            val initialAmount = BigDecimal.valueOf(1_000)
            val balance = BalanceMock.balance(amount = initialAmount)
            val useAmount = BigDecimal.valueOf(500)

            val result = balance.use(BalanceAmount.of(useAmount))

            assertThat(result.balanceId).isEqualTo(balance.id())
            assertThat(result.amount.value).isEqualByComparingTo(useAmount)
            assertThat(balance.amount.value).isEqualByComparingTo(initialAmount.minus(useAmount))
            assertThat(balance.updatedAt).isAfter(balance.createdAt)
        }
    }
}
