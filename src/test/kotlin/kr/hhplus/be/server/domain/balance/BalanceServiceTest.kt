package kr.hhplus.be.server.domain.balance

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.balance.command.ChargeBalanceCommand
import kr.hhplus.be.server.domain.balance.command.UseBalanceCommand
import kr.hhplus.be.server.domain.balance.exception.ExceedMaxBalanceAmountException
import kr.hhplus.be.server.domain.balance.exception.InsufficientBalanceException
import kr.hhplus.be.server.domain.balance.exception.NotFoundBalanceException
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
    fun `charge - 유저 Id에 해당하는 Balance가 없으면 충전 금액을 가진 잔고를 생성한다`() {
        val userId = UserMock.id()
        val command = ChargeBalanceCommand(userId = userId, amount = BalanceMock.amount().value)
        val newBalanceId = BalanceMock.id()
        every { repository.findByUserId(userId) } returns null
        every { repository.save(any()) } returns newBalanceId

        val result = service.charge(command)

        assertAll(
            { assertThat(result.balanceId).isEqualTo(newBalanceId) },
        )
        assertThat(result.balanceId).isEqualTo(newBalanceId)
        verify {
            repository.save(withArg<Balance> {
                assertThat(it.userId).isEqualTo(userId)
                assertThat(it.amount).isEqualByComparingTo(command.amount)
            })
        }
    }

    @Test
    fun `charge - 유저 Id에 해당하는 Balance가 있으면 충전 금액을 더한 후 업데이트 한다`() {
        val existingBalanceId = BalanceMock.id()
        val existingBalance = BalanceMock.balance(id = existingBalanceId, amount = BigDecimal.ONE)
        val originalAmount = existingBalance.amount
        val chargeAmount = BigDecimal.valueOf(1_000)
        val command = ChargeBalanceCommand(userId = existingBalance.userId, amount = chargeAmount)
        every { repository.findByUserId(command.userId) } returns existingBalance
        every { repository.save(any()) } returns existingBalanceId

        val result = service.charge(command)

        assertThat(result.balanceId).isEqualTo(existingBalance.id)
        verify {
            repository.save(withArg<Balance> {
                assertThat(it.id).isEqualTo(existingBalance.id)
                assertThat(it.amount).isEqualByComparingTo(originalAmount.add(chargeAmount))
            })
        }
    }

    @Test
    fun `charge - 충전 중 예외가 발생하면 해당 예외를 그대로 반환하고 잔고를 업데이트 하지 않는다`() {
        val userId = UserMock.id()
        val balance = mockk<Balance>()
        val command = ChargeBalanceCommand(userId = userId, amount = BalanceMock.amount().value)
        every { repository.findByUserId(command.userId) } returns balance
        every { balance.charge(BalanceAmount(command.amount)) } throws ExceedMaxBalanceAmountException(BigDecimal.valueOf(1_000))

        assertThrows<ExceedMaxBalanceAmountException> {
            service.charge(command)
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `use - 잔고 사용`() {
        val existingBalanceId = BalanceMock.id()
        val existingBalance = BalanceMock.balance(id = existingBalanceId, amount = BigDecimal.valueOf(3_000))
        val originalAmount = existingBalance.amount
        val useAmount = BigDecimal.valueOf(1_000)
        val command = UseBalanceCommand(userId = existingBalance.userId, amount = useAmount)
        every { repository.findByUserId(command.userId) } returns existingBalance

        service.use(command)

        verify {
            repository.save(withArg<Balance> {
                assertThat(it.id).isEqualTo(existingBalance.id)
                assertThat(it.amount).isEqualByComparingTo(originalAmount.minus(useAmount))
                assertThat(it.updatedAt).isAfter(existingBalance.createdAt)
            })
        }
    }

    @Test
    fun `use - 유저 Id에 해당하는 Balance가 없음 - NotFoundBalanceException`() {
        val userId = UserMock.id()
        val command = UseBalanceCommand(userId = userId, amount = BalanceMock.amount().value)
        every { repository.findByUserId(userId) } returns null

        shouldThrow<NotFoundBalanceException> {
            service.use(command)
        }
    }

    @Test
    fun `use - 사용 중 예외가 발생하면 해당 예외를 그대로 반환하고 잔고를 업데이트 하지 않는다`() {
        val userId = UserMock.id()
        val balance = mockk<Balance>()
        val command = UseBalanceCommand(userId = userId, amount = BalanceMock.amount().value)
        every { repository.findByUserId(command.userId) } returns balance
        every { balance.use(BalanceAmount(command.amount)) } throws InsufficientBalanceException(BalanceMock.id().value, BigDecimal.valueOf(1_000), command.amount)

        assertThrows<InsufficientBalanceException> {
            service.use(command)
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `getOrNullByUerId - 유저 Id로 잔고를 조회해 반환한다`() {
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
    fun `getOrNullByUerId - 해당 유저의 잔고가 없는 경우, null이 반환된다`() {
        val userId = UserMock.id()
        every { repository.findByUserId(userId) } returns null

        val result = service.getOrNullByUerId(userId)

        assertThat(result).isNull()
        verify { repository.findByUserId(userId) }
    }

    @Test
    fun `get - Id로 잔고를 조회해 반환한다`() {
        val balanceId = BalanceMock.id()
        val balance = BalanceMock.balance(id = balanceId)
        every { repository.findById(balanceId.value) } returns balance

        val result = service.get(balanceId.value)

        assertAll(
            { assertThat(result.id).isEqualTo(balance.id) },
            { assertThat(result.userId).isEqualTo(balance.userId) },
            { assertThat(result.amount).isEqualByComparingTo(balance.amount) },
            { assertThat(result.createdAt).isEqualTo(balance.createdAt) },
            { assertThat(result.updatedAt).isEqualTo(balance.updatedAt) }
        )
        verify { repository.findById(balanceId.value) }
    }

    @Test
    fun `getOrNullByUerId - 해당 잔고가 없는 경우 - NotFoundBalanceException`() {
        val balanceId = BalanceMock.id()
        every { repository.findById(balanceId.value) } returns null

        shouldThrow<NotFoundBalanceException> {
            service.get(balanceId.value)
        }
    }
}
