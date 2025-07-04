import kr.hhplus.be.server.application.balance.result.BalanceChargeFacadeResult
import kr.hhplus.be.server.application.balance.result.GetBalanceFacadeResult
import kr.hhplus.be.server.interfaces.balance.api.response.BalanceResponse
import kr.hhplus.be.server.testutil.mock.BalanceMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.math.BigDecimal

class BalanceResponseTest {

    @Nested
    @DisplayName("응답 생성")
    inner class Create {
        @Test
        @DisplayName("잔고 조회 결과로부터 응답 생성")
        fun found() {
            val balance = BalanceMock.view()
            val result = GetBalanceFacadeResult.Found(value = balance)

            val response = BalanceResponse.of(result)

            assertAll(
                { assertThat(response.userId).isEqualTo(balance.userId.value) },
                { assertThat(response.balance).isEqualByComparingTo(balance.amount) }
            )
        }

        @Test
        @DisplayName("빈 잔고 조회 결과로부터 기본값 응답 생성")
        fun empty() {
            val userId = UserMock.id()
            val result = GetBalanceFacadeResult.Empty(userId = userId)

            val response = BalanceResponse.of(result)

            assertAll(
                { assertThat(response.userId).isEqualTo(userId.value) },
                { assertThat(response.balance).isEqualByComparingTo(BigDecimal.ZERO) }
            )
        }

        @Test
        @DisplayName("충전 결과로부터 응답 생성")
        fun charge() {
            val balance = BalanceMock.view()
            val result = BalanceChargeFacadeResult(balance = balance)

            val response = BalanceResponse.of(result)

            assertAll(
                { assertThat(response.userId).isEqualTo(balance.userId.value) },
                { assertThat(response.balance).isEqualByComparingTo(balance.amount) }
            )
        }
    }
}
