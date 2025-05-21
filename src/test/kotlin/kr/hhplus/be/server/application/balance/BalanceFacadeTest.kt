package kr.hhplus.be.server.application.balance

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.application.balance.command.ChargeBalanceFacadeCommand
import kr.hhplus.be.server.application.balance.result.BalanceChargeFacadeResult
import kr.hhplus.be.server.application.balance.result.GetBalanceFacadeResult
import kr.hhplus.be.server.domain.balance.BalanceService
import kr.hhplus.be.server.domain.balance.command.ChargeBalanceCommand
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.domain.user.exception.NotFoundUserException
import kr.hhplus.be.server.testutil.mock.BalanceMock
import kr.hhplus.be.server.testutil.mock.IdMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class BalanceFacadeTest {
    private val balanceService = mockk<BalanceService>(relaxed = true)
    private val userService = mockk<UserService>(relaxed = true)
    private val facade = BalanceFacade(balanceService, userService)

    @BeforeEach
    fun setup() {
        clearMocks(balanceService, userService)
    }

    @Test
    @DisplayName("충전 - 해당 유저의 잔고를 충전한다")
    fun charge() {
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
    @DisplayName("충전 - 충전 결과로 BalanceChargeResult를 반환한다")
    fun verifyChargeResult() {
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

        Assertions.assertThat(result).isEqualTo(BalanceChargeFacadeResult(chargedBalance))
    }

    @Test
    @DisplayName("충전 - 유저 조회에 실패할 경우 발생한 예외를 던지며, 충전을 하지 않는다")
    fun chargeFailIfNotExistUser() {
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
    @DisplayName("유저 ID로 잔고 조회 - 잔고가 있는 경우 GetBalanceFacadeResult.Found 결과를 반환한다")
    fun getExistsBalance() {
        val userId = UserMock.id()
        val balance = BalanceMock.view(userId = userId)
        every { userService.get(userId.value) } returns UserMock.view(id = userId)
        every { balanceService.getOrNullByUserId(userId) } returns balance

        val result = facade.getOrNullByUerId(userId.value)

        Assertions.assertThat(result).isInstanceOf(GetBalanceFacadeResult.Found::class.java)
        Assertions.assertThat((result as GetBalanceFacadeResult.Found).value).isEqualTo(balance)
    }

    @Test
    @DisplayName("유저 ID로 잔고 조회 - 잔고가 없는 경우 Empty 결과를 반환한다")
    fun getNotExistsBalance() {
        val userId = UserMock.id()
        every { userService.get(userId.value) } returns UserMock.view(id = userId)
        every { balanceService.getOrNullByUserId(userId) } returns null

        val result = facade.getOrNullByUerId(userId.value)

        Assertions.assertThat(result).isInstanceOf(GetBalanceFacadeResult.Empty::class.java)
        Assertions.assertThat((result as GetBalanceFacadeResult.Empty).userId).isEqualTo(userId)
    }

    @Test
    @DisplayName("유저 ID로 잔고 조회 - 유저 조회에 실패할 경우 발생한 예외를 던진다")
    fun getBalanceNotExistsUser() {
        val userId = IdMock.value()
        every { userService.get(userId) } throws NotFoundUserException("by id: $userId")

        assertThrows<NotFoundUserException> {
            facade.getOrNullByUerId(userId)
        }
    }
}
