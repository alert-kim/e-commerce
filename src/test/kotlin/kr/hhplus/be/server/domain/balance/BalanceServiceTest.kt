package kr.hhplus.be.server.domain.balance

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.balance.command.ChargeBalanceCommand
import kr.hhplus.be.server.domain.balance.exception.ExceedMaxBalanceException
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
class BalanceServiceTest {
    @InjectMockKs
    private lateinit var service: BalanceService

    @MockK(relaxed = true)
    private lateinit var repository: BalanceRepository

    @BeforeEach
    fun setUp() {
        clearMocks(repository)
    }

    @Test
    fun `조회 - 유저 Id로 잔고를 조회해 반환한다`() {
        val userId = UserMock.id()
        val balance = BalanceMock.balance(userId = userId)
        every { repository.findByUserId(userId) } returns balance

        val result = service.getOrNullByUerId(userId)

        assertAll(
            { assertThat(result).isNotNull() },
            { assertThat(result?.id).isEqualTo(balance.id) },
            { assertThat(result?.userId).isEqualTo(balance.userId) },
            { assertThat(result?.amount).isEqualByComparingTo(balance.amount) },
            { assertThat(result?.createdAt).isEqualTo(balance.createdAt) },
            { assertThat(result?.updatedAt).isEqualTo(balance.updatedAt) }
        )
        verify { repository.findByUserId(userId) }
    }

    @Test
    fun `조회 - 해당 유저의 잔고가 없는 경우, null이 반환된다`() {
        val userId = UserMock.id()
        every { repository.findByUserId(userId) } returns null

        val result = service.getOrNullByUerId(userId)

        assertThat(result).isNull()
        verify { repository.findByUserId(userId) }
    }

    @Test
    fun `충전 - 유저 Id에 해당하는 Balance가 없으면 충전 금액을 가진 잔고를 생성한다`() {
        val userId = UserMock.id()
        val command = ChargeBalanceCommand(userId = userId, amount = BalanceMock.amount().value)
        val newBalanceId = BalanceMock.id()
        every { repository.findByUserId(userId) } returns null
        every { repository.save(any()) } returns newBalanceId

        val result = service.charge(command)

        assertAll(
            { assertThat(result).isEqualTo(newBalanceId) },
        )
        assertThat(result).isEqualTo(newBalanceId)
        verify {
            repository.save(withArg<Balance> {
                assertThat(it.userId).isEqualTo(userId)
                assertThat(it.amount).isEqualByComparingTo(command.amount)
            })
        }
    }

    @Test
    fun `충전 - 유저 Id에 해당하는 Balance가 있으면 충전 금액을 더한 후 업데이트 한다`() {
        val existingBalanceId = BalanceMock.id()
        val existingBalance = BalanceMock.balance(id = existingBalanceId, amount = BigDecimal.ONE)
        val originalAmount = existingBalance.amount
        val chargeAmount = BigDecimal.valueOf(1_000)
        val command = ChargeBalanceCommand(userId = existingBalance.userId, amount = chargeAmount)
        every { repository.findByUserId(command.userId) } returns existingBalance
        every { repository.save(any()) } returns existingBalanceId

        val result = service.charge(command)

        assertThat(result).isEqualTo(existingBalance.id)
        verify {
            repository.save(withArg<Balance> {
                assertThat(it.id).isEqualTo(existingBalance.id)
                assertThat(it.amount).isEqualByComparingTo(originalAmount.add(chargeAmount))
            })
        }
    }

    @Test
    fun `충전 - 충전 중 예외가 발생하면 해당 예외를 그대로 반환하고 잔고를 업데이트 하지 않는다`() {
        val userId = UserMock.id()
        val balance = mockk<Balance>()
        val command = ChargeBalanceCommand(userId = userId, amount = BalanceMock.amount().value)
        every { repository.findByUserId(command.userId) } returns balance
        every { balance.charge(BalanceAmount(command.amount)) } throws ExceedMaxBalanceException(BigDecimal.valueOf(1_000))

        assertThrows<ExceedMaxBalanceException> {
            service.charge(command)
        }

        verify(exactly = 0) { repository.save(any()) }
    }
}
