package kr.hhplus.be.server.domain.balance.dto

import io.kotest.assertions.throwables.shouldThrow
import kr.hhplus.be.server.domain.balance.Balance
import kr.hhplus.be.server.domain.balance.BalanceId
import kr.hhplus.be.server.domain.balance.exception.RequiredBalanceIdException
import kr.hhplus.be.server.domain.user.UserId
import kr.hhplus.be.server.mock.BalanceMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class BalanceQueryModelTest {

    @Test
    fun `from - Balance 객체를 올바르게 변환한다`() {
        val balance = BalanceMock.balance()

        val result = BalanceQueryModel.from(balance)

        assertAll(
            { assertThat(result).isNotNull() },
            { assertThat(result.id).isEqualTo(balance.id) },
            { assertThat(result.userId).isEqualTo(balance.userId) },
            { assertThat(result.amount).isEqualByComparingTo(balance.amount) },
            { assertThat(result.createdAt).isEqualTo(balance.createdAt) },
            { assertThat(result.updatedAt).isEqualTo(balance.updatedAt) }
        )
    }

    @Test
    fun `from - Balance id가 널인 객체는 변환시키지 못하고 RequiredBalanceIdException가 발생한다`() {
        val balance = BalanceMock.balance(id = null)

        shouldThrow<RequiredBalanceIdException> {
            BalanceQueryModel.from(balance)
        }
    }
}
