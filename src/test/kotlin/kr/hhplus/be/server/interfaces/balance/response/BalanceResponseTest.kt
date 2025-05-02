import kr.hhplus.be.server.application.balance.result.BalanceResult
import kr.hhplus.be.server.interfaces.balance.response.BalanceResponse
import kr.hhplus.be.server.testutil.mock.BalanceMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.math.BigDecimal

class BalanceResponseTest {

    @Test
    fun `Found 결과로부터 응답 생성`() {
        val balance = BalanceMock.view()
        val result = BalanceResult.Found(value = balance)

        val response = BalanceResponse.of(result)

        assertAll(
            { assertThat(response.userId).isEqualTo(balance.userId.value) },
            { assertThat(response.balance).isEqualByComparingTo(balance.amount) }
        )
    }

    @Test
    fun `Empty 결과로부터 기본값으로 응답 생성`() {
        val userId = UserMock.id()
        val result = BalanceResult.Empty(userId = userId)

        val response = BalanceResponse.of(result)

        assertAll(
            { assertThat(response.userId).isEqualTo(userId.value) },
            { assertThat(response.balance).isEqualByComparingTo(BigDecimal.ZERO) }
        )
    }
}
