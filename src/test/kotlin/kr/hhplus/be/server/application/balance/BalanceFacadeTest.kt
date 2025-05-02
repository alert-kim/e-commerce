package kr.hhplus.be.server.application.balance

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.application.balance.command.ChargeBalanceFacadeCommand
import kr.hhplus.be.server.application.balance.result.BalanceResult
import kr.hhplus.be.server.domain.balance.BalanceService
import kr.hhplus.be.server.domain.balance.command.ChargeBalanceCommand
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.domain.user.exception.NotFoundUserException
import kr.hhplus.be.server.testutil.mock.BalanceMock
import kr.hhplus.be.server.testutil.mock.IdMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class BalanceFacadeTest {
    @InjectMockKs
    private lateinit var facade: BalanceFacade

    @MockK(relaxed = true)
    private lateinit var balanceService: BalanceService

    @MockK(relaxed = true)
    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        clearMocks(balanceService, userService)
    }

    @Test
    fun `충전 - 해당 유저의 잔고를 충전한다`() {
        val userId = UserMock.id()
        val balanceId = BalanceMock.id()
        val command = ChargeBalanceFacadeCommand(amount = BigDecimal.valueOf(1_000), userId = userId.value)
        every { userService.get(userId.value) } returns UserMock.view(id = userId)
        every {
            balanceService.charge(
                ChargeBalanceCommand(
                    userId = userId,
                    amount = command.amount,
                )
            )
        } returns balanceId

        facade.charge(command)

        verify {
            balanceService.charge(
                ChargeBalanceCommand(
                    userId = userId,
                    amount = command.amount,
                )
            )
        }
    }

    @Test
    fun `충전 - 충전 결과로 Found를 반환한다`() {
        val userId = UserMock.id()
        val balanceId = BalanceMock.id()
        val command = ChargeBalanceFacadeCommand(amount = BigDecimal.valueOf(1_000), userId = userId.value)
        val chargedBalance = BalanceMock.view(id = balanceId, userId = userId)
        every { userService.get(userId.value) } returns UserMock.view(id = userId)
        every {
            balanceService.charge(ofType(ChargeBalanceCommand::class))
        } returns balanceId
        every { balanceService.get(balanceId.value) } returns chargedBalance

        val result = facade.charge(command)

        assertThat(result).isEqualTo(BalanceResult.Found(chargedBalance))
    }

    @Test
    fun `충전 - 유저 조회에 실패할 경우 발생한 예외를 던지며, 충전을 하지 않는다`() {
        val userId = IdMock.value()
        val command = ChargeBalanceFacadeCommand(amount = BigDecimal.valueOf(1_000), userId = userId)
        every { userService.get(userId) } throws NotFoundUserException("by id: $userId")

        assertThrows<NotFoundUserException> {
            facade.charge(command)
        }

        verify(exactly = 0) {
            balanceService.charge(any())
        }
    }

    @Test
    fun `유저 ID로 잔고 조회 - 잔고가 있는 경우 Found 결과를 반환한다`() {
        val userId = UserMock.id()
        val balance = BalanceMock.view(userId = userId)
        every { userService.get(userId.value) } returns UserMock.view(id = userId)
        every { balanceService.getOrNullByUserId(userId) } returns balance

        val result = facade.getOrNullByUerId(userId.value)

        assertThat(result).isInstanceOf(BalanceResult.Found::class.java)
        assertThat((result as BalanceResult.Found).value).isEqualTo(balance)
    }

    @Test
    fun `유저 ID로 잔고 조회 - 잔고가 없는 경우 Empty 결과를 반환한다`() {
        val userId = UserMock.id()
        every { userService.get(userId.value) } returns UserMock.view(id = userId)
        every { balanceService.getOrNullByUserId(userId) } returns null

        val result = facade.getOrNullByUerId(userId.value)

        assertThat(result).isInstanceOf(BalanceResult.Empty::class.java)
        assertThat((result as BalanceResult.Empty).userId).isEqualTo(userId)
    }

    @Test
    fun `유저 ID로 잔고 조회 - 유저 조회에 실패할 경우 발생한 예외를 던지며, 충전을 하지 않는다`() {
        val userId = IdMock.value()
        every { userService.get(userId) } throws NotFoundUserException("by id: $userId")

        assertThrows<NotFoundUserException> {
            facade.getOrNullByUerId(userId)
        }

        verify(exactly = 0) {
            balanceService.charge(any())
        }
    }
}
