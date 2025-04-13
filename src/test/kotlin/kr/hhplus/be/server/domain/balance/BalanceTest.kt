package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.balance.exception.RequiredBalanceIdException
import kr.hhplus.be.server.mock.BalanceMock
import kr.hhplus.be.server.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

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

    @Test
    fun `new - 해당 유저아이디를 가진, 0원으로 초기화된 잔고를 생성한다`() {
        val userId = UserMock.id()

        val balance = Balance.new(userId)

        assertAll(
            { assertThat(balance.userId).isEqualTo(userId) },
            { assertThat(balance.amount).isEqualByComparingTo(BigDecimal.ZERO) },
        )
    }

    @Test
    fun `charge - 잔고를 충전하고 updatedAt을 갱신한다`() {
        val initialAmount = BigDecimal.valueOf(1_000)
        val balance = BalanceMock.balance(amount = initialAmount)
        val chargeAmount = BigDecimal.valueOf(2_000)

        balance.charge(BalanceAmount(chargeAmount))

        assertThat(balance.amount).isEqualByComparingTo(initialAmount.add(chargeAmount))
        assertThat(balance.updatedAt).isAfter(balance.createdAt)
    }

    @Test
    fun `user - 잔고를 사용하고 updatedAt을 갱신한다`() {
        val initialAmount = BigDecimal.valueOf(1_000)
        val balance = BalanceMock.balance(amount = initialAmount)
        val useAmount = BigDecimal.valueOf(500)

        balance.use(BalanceAmount(useAmount))

        assertThat(balance.amount).isEqualByComparingTo(initialAmount.minus(useAmount))
        assertThat(balance.updatedAt).isAfter(balance.createdAt)
    }
}
