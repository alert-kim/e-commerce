package kr.hhplus.be.server.domain.balance

import io.kotest.assertions.throwables.shouldThrow
import kr.hhplus.be.server.domain.balance.exception.RequiredBalanceIdException
import kr.hhplus.be.server.testutil.mock.BalanceMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

@DisplayName("잔고 뷰 테스트")
class BalanceViewTest {

    @Nested
    @DisplayName("변환")
    inner class Convert {
        @Test
        @DisplayName("Balance를 올바르게 변환")
        fun fromBalance() {
            val balance = BalanceMock.balance()

            val result = BalanceView.from(balance)

            assertAll(
                { assertThat(result).isNotNull() },
                { assertThat(result.id).isEqualTo(balance.id()) },
                { assertThat(result.userId).isEqualTo(balance.userId) },
                { assertThat(result.amount).isEqualByComparingTo(balance.amount.value) },
                { assertThat(result.createdAt).isEqualTo(balance.createdAt) },
                { assertThat(result.updatedAt).isEqualTo(balance.updatedAt) }
            )
        }

        @Test
        @DisplayName("ID가 없으면 예외 발생")
        fun fromBalanceWithNullId() {
            val balance = BalanceMock.balance(id = null)

            shouldThrow<RequiredBalanceIdException> {
                BalanceView.from(balance)
            }
        }
    }
}
