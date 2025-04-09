package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.balance.exception.RequiredBalanceIdException
import kr.hhplus.be.server.mock.BalanceMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BalanceTest {
    @Test
    fun `requireId - id가 null이 아닌 경우 id 반환`() {
        val balance = BalanceMock.balance(id = BalanceMock.id())

        val result = balance.requireId()

        assertThat(result).isEqualTo(balance.id)
    }

    @Test
    fun `requireId - id가 null이면 RequiredBalanceIdException 발생`() {
        val balance = BalanceMock.balance(id = null)

        assertThrows<RequiredBalanceIdException> {
            balance.requireId()
        }
    }
}
