package kr.hhplus.be.server.interfaces.balance.response

import kr.hhplus.be.server.mock.BalanceMock
import kr.hhplus.be.server.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.math.BigDecimal

class BalanceResponseTest {

    @Test
    fun `잔액에 대한 응답 생성`() {
        val balance = BalanceMock.queryModel()

        val response = BalanceResponse.of(balance.userId, balance)

        assertAll(
            { assertThat(response.userId).isEqualTo(balance.userId.value) },
            { assertThat(response.amount).isEqualByComparingTo(balance.amount) },
            { assertThat(response.createdAt).isEqualTo(balance.createdAt) },
            { assertThat(response.updatedAt).isEqualTo(balance.updatedAt) }
        )
    }

    @Test
    fun `잔액이 없는 경우 기본값으로 응답 생성`() {
        val userId = UserMock.id()

        val response = BalanceResponse.of(userId, null)

        assertAll(
            { assertThat(response.userId).isEqualTo(userId.value) },
            { assertThat(response.amount).isEqualByComparingTo(BigDecimal.ZERO) }
        )
    }
}
