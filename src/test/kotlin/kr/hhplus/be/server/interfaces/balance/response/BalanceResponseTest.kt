import kr.hhplus.be.server.application.balance.result.BalanceChargeFacadeResult
import kr.hhplus.be.server.application.balance.result.GetBalanceFacadeResult
import kr.hhplus.be.server.interfaces.balance.response.BalanceResponse
import kr.hhplus.be.server.testutil.mock.BalanceMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.math.BigDecimal

class BalanceResponseTest {

    @Test
    @DisplayName("GetBalanceFacadeResult.Found 결과로부터 응답 생성")
    fun ofGetBalanceFacadeResultFound() {
        val balance = BalanceMock.view()
        val result = GetBalanceFacadeResult.Found(value = balance)

        val response = BalanceResponse.of(result)

        assertAll(
            { assertThat(response.userId).isEqualTo(balance.userId.value) },
            { assertThat(response.balance).isEqualByComparingTo(balance.amount) }
        )
    }

    @Test
    @DisplayName("GetBalanceFacadeResult.Empty 결과로부터 기본값으로 응답 생성")
    fun ofGetBalanceFacadeResultEmpty() {
        val userId = UserMock.id()
        val result = GetBalanceFacadeResult.Empty(userId = userId)

        val response = BalanceResponse.of(result)

        assertAll(
            { assertThat(response.userId).isEqualTo(userId.value) },
            { assertThat(response.balance).isEqualByComparingTo(BigDecimal.ZERO) }
        )
    }

    @Test
    @DisplayName("BalanceChargeFacadeResult 결과로부터 기본값으로 응답 생성")
    fun ofBalanceChargeFacadeResult() {
        val balance = BalanceMock.view()
        val result = BalanceChargeFacadeResult(balance = balance)

        val response = BalanceResponse.of(result)

        assertAll(
            { assertThat(response.userId).isEqualTo(balance.userId.value) },
            { assertThat(response.balance).isEqualByComparingTo(balance.amount) }
        )
    }
}
