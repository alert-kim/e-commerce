package kr.hhplus.be.server.application.balance

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.application.balance.command.ChargeBalanceFacadeCommand
import kr.hhplus.be.server.application.balance.result.ChargeBalanceResult
import kr.hhplus.be.server.domain.balance.BalanceRecordService
import kr.hhplus.be.server.domain.balance.BalanceService
import kr.hhplus.be.server.domain.balance.command.ChargeBalanceCommand
import kr.hhplus.be.server.domain.balance.exception.ExceedMaxBalanceException
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.domain.user.exception.NotFoundUserException
import kr.hhplus.be.server.mock.BalanceMock
import kr.hhplus.be.server.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
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
    private lateinit var balanceRecordService: BalanceRecordService

    @MockK(relaxed = true)
    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        clearMocks(balanceService, balanceRecordService, userService)
    }

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

    @Test
    fun `충전 - 해당 잔고에 충전 후에 잔고를 반환한다`() {
        val userId = UserMock.id()
        val balanceId = BalanceMock.id()
        val command = ChargeBalanceFacadeCommand(amount = BigDecimal.valueOf(1_000), userId = userId.value)
        val chargedBalance = BalanceMock.balance(id = balanceId, userId = userId)
        every { userService.get(userId.value) } returns UserMock.user(id = userId)
        every {
            balanceService.charge(
                ChargeBalanceCommand(
                    userId = userId,
                    amount = command.amount,
                )
            )
        } returns balanceId
        every { balanceService.get(balanceId.value) } returns chargedBalance

        val result = facade.charge(command)

        assertAll(
            { assertThat(result.id).isEqualTo(chargedBalance.id) },
            { assertThat(result.userId).isEqualTo(chargedBalance.userId) },
            { assertThat(result.amount).isEqualByComparingTo(chargedBalance.amount) },
            { assertThat(result.createdAt).isEqualTo(chargedBalance.createdAt) },
            { assertThat(result.updatedAt).isEqualTo(chargedBalance.updatedAt) }
        )
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
    fun `충전 - 유저 조회에 실패할 경우 발생한 예외를 던지며, 충전을 하지 않고, 기록을 남기지 않는다`() {
        val userId = UserMock.id()
        val command = ChargeBalanceFacadeCommand(amount = BigDecimal.valueOf(1_000), userId = userId.value)
        every { userService.get(userId.value) } throws NotFoundUserException("by id: ${userId.value}")


        assertThrows<NotFoundUserException> {
            facade.charge(command)
        }

        verify(exactly = 0) {
            balanceService.charge(any())
            balanceRecordService.record(any())
        }
    }

    @Test
    fun `충전 - 잔고 충전에 실패할 경우(잔고 초과) 발생한 예외를 던지며, 충전을 하지 않고, 기록도 남기지 않는다`() {
        val userId = UserMock.id()
        val command = ChargeBalanceFacadeCommand(amount = BigDecimal.valueOf(1_000), userId = userId.value)
        every { userService.get(userId.value) } returns UserMock.user(id = userId)
        every { balanceService.charge(ChargeBalanceCommand(userId, command.amount)) } throws ExceedMaxBalanceException(
            BalanceMock.id(),
            command.amount
        )

        assertThrows<ExceedMaxBalanceException> {
            facade.charge(command)
        }

        verify(exactly = 0) {
            balanceRecordService.record(any())
        }
        verify(exactly = 0) { balanceRecordService.record(any()) }
    }
}
