package kr.hhplus.be.server.domain.balance

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.mock.BalanceMock
import kr.hhplus.be.server.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class BalanceFacadeTest {
    @InjectMockKs
    private lateinit var facade: BalanceFacade

    @MockK(relaxed = true)
    private lateinit var balanceService: BalanceService

    @Test
    fun `조회 - 유저 Id로 잔고를 조회해 반환한다`() {
        val userId = UserMock.id()
        val balance = BalanceMock.balance(userId = userId)
        every { balanceService.getOrNullByUerId(userId) } returns balance

        val result = facade.getOrNullByUerId(userId)

        assertAll(
            { assertThat(result).isNotNull() },
            { assertThat(result?.id).isEqualTo(balance.id) },
            { assertThat(result?.userId).isEqualTo(balance.userId) },
            { assertThat(result?.amount).isEqualByComparingTo(balance.amount) },
            { assertThat(result?.createdAt).isEqualTo(balance.createdAt) },
            { assertThat(result?.updatedAt).isEqualTo(balance.updatedAt) }
        )
        verify { balanceService.getOrNullByUerId(userId) }
    }

    @Test
    fun `조회 - 해당 유저의 잔고가 없는 경우, null이 반환된다`() {
        val userId = UserMock.id()
        every { balanceService.getOrNullByUerId(userId) } returns null

        val result = facade.getOrNullByUerId(userId)

        assertThat(result).isNull()
        verify { balanceService.getOrNullByUerId(userId) }
    }
}
