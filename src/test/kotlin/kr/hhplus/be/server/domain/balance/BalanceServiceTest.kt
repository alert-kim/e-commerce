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
import kr.hhplus.be.server.domain.balance.exception.BelowMinBalanceAmountException
import kr.hhplus.be.server.domain.balance.exception.ExceedMaxBalanceAmountException
import kr.hhplus.be.server.domain.balance.exception.NotFoundBalanceException
import kr.hhplus.be.server.domain.balance.repository.BalanceRecordRepository
import kr.hhplus.be.server.domain.balance.repository.BalanceRepository
import kr.hhplus.be.server.domain.balance.result.UsedBalanceAmount
import kr.hhplus.be.server.testutil.mock.BalanceMock
import kr.hhplus.be.server.testutil.mock.IdMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
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

    @MockK(relaxed = true)
    private lateinit var recordRepository: BalanceRecordRepository

    @BeforeEach
    fun setUp() {
        clearMocks(repository, recordRepository)
    }

    @Test
    @DisplayName("charge - 유저 Id에 해당하는 Balance가 없으면 잔고를 생성해 충전 후, 잔고 Id를 반환한다")
    fun chargeNotYetCreatedBalance() {
        val userId = UserMock.id()
        val balanceId = BalanceMock.id()
        val balance = mockk<Balance>(relaxed = true)
        val command = ChargeBalanceCommand(userId = userId, amount = BalanceMock.amount().value)
        every { repository.findByUserId(userId) } returns null
        every { repository.save(any()) } returns balance
        every { balance.id() } returns balanceId

        val result = service.charge(command)

        assertThat(result).isEqualTo(balanceId)
        verify {
            repository.save(withArg<Balance> {
                assertThat(it.userId).isEqualTo(command.userId)
            })

            balance.charge(BalanceAmount.of(command.amount))
        }
    }

    @Test
    @DisplayName("charge - 유저 Id에 해당하는 잔고가 있으면 충전 후, 해당 잔고 Id가 반환된다")
    fun chargeExistsBalance() {
        val existingBalanceId = BalanceMock.id()
        val existingBalance = mockk<Balance>(relaxed = true)
        val command = ChargeBalanceCommand(userId = existingBalance.userId, amount = 1000.toBigDecimal())
        every { existingBalance.id() } returns existingBalanceId
        every { repository.findByUserId(command.userId) } returns existingBalance

        val result = service.charge(command)

        assertThat(result).isEqualTo(existingBalanceId)
        verify {
            existingBalance.charge(BalanceAmount.of(command.amount))
        }
    }

    @Test
    @DisplayName("charge - 잔고를 충전 후, 레코드를 생성한다")
    fun createRecordWhenCharging() {
        val userId = UserMock.id()
        val balance = BalanceMock.balance(id = BalanceMock.id(), amount = BigDecimal.ZERO)
        val command = ChargeBalanceCommand(userId = userId, amount = BalanceMock.amount().value)
        every { repository.findByUserId(userId) } returns balance

        service.charge(command)

        verify {
            recordRepository.save(
                withArg<BalanceRecord> {
                    assertThat(it.balanceId).isEqualTo(balance.id())
                    assertThat(it.type).isEqualTo(BalanceTransactionType.CHARGE)
                    assertThat(it.amount.value).isEqualByComparingTo(command.amount)
                }
            )
        }
    }

    @Test
    @DisplayName("charge - 충전 중 예외가 발생하면 해당 예외를 그대로 반환하고 레코드를 생성 하지 않는다")
    fun errorWhenCharging() {
        val userId = UserMock.id()
        val balance = mockk<Balance>()
        val command = ChargeBalanceCommand(userId = userId, amount = BalanceMock.amount().value)
        every { repository.findByUserId(command.userId) } returns balance
        every { balance.charge(any()) } throws ExceedMaxBalanceAmountException(1_000.toBigDecimal())

        assertThrows<ExceedMaxBalanceAmountException> {
            service.charge(command)
        }

        verify(exactly = 0) {
            recordRepository.save(any())
        }
    }

    @Test
    @DisplayName("use - 잔고 사용 후 잔고를 업데이트 한다")
    fun use() {
        val existingBalanceId = BalanceMock.id()
        val existingBalance = mockk<Balance>(relaxed = true)
        val command = UseBalanceCommand(userId = UserMock.id(), amount = BigDecimal.valueOf(1_000))
        every { existingBalance.id() } returns existingBalanceId
        every { repository.findByUserId(command.userId) } returns existingBalance

        service.use(command)

        verify {
            existingBalance.use(BalanceAmount.of(command.amount))
        }
    }

    @Test
    @DisplayName("use - 잔고 사용 후 사용된 잔고 정보가 반환된다")
    fun verifyUseResult() {
        val existingBalanceId = BalanceMock.id()
        val existingBalance = mockk<Balance>(relaxed = true)
        val usedAmount = UsedBalanceAmount(existingBalanceId, BalanceAmount.of(1_000.toBigDecimal()))
        val command = UseBalanceCommand(userId = UserMock.id(), amount = BigDecimal.valueOf(1_000))
        every { existingBalance.id() } returns existingBalanceId
        every { existingBalance.use(any()) } returns usedAmount
        every { repository.findByUserId(command.userId) } returns existingBalance

        val result = service.use(command)

        assertThat(result).isEqualTo(usedAmount)
    }

    @Test
    @DisplayName("use - 잔고 사용 후 레코드를 생성한다")
    fun createRecordWhenUsing() {
        val existingBalanceId = BalanceMock.id()
        val existingBalance = mockk<Balance>(relaxed = true)
        val command = UseBalanceCommand(userId = UserMock.id(), amount = BigDecimal.valueOf(1_000))
        every { existingBalance.id() } returns existingBalanceId
        every { repository.findByUserId(command.userId) } returns existingBalance

        service.use(command)

        verify {
            recordRepository.save(withArg<BalanceRecord> {
                assertThat(it.balanceId).isEqualTo(existingBalanceId)
                assertThat(it.amount.value).isEqualByComparingTo(command.amount)
            })
        }
    }

    @Test
    @DisplayName("use - 유저 Id에 해당하는 Balance가 없음 - NotFoundBalanceException")
    fun useNotExistsBalance() {
        val userId = UserMock.id()
        val command = UseBalanceCommand(userId = userId, amount = BalanceMock.amount().value)
        every { repository.findByUserId(userId) } returns null

        shouldThrow<NotFoundBalanceException> {
            service.use(command)
        }
    }

    @Test
    @DisplayName("use - 잔고 사용 중 예외가 발생하면 해당 예외를 그대로 반환하고 레코드를 생성하지 않는다")
    fun errorWhenUsing() {
        val userId = UserMock.id()
        val balance = mockk<Balance>()
        val command = UseBalanceCommand(userId = userId, amount = BalanceMock.amount().value)
        every { repository.findByUserId(command.userId) } returns balance
        every { balance.use(BalanceAmount.of(command.amount)) } throws BelowMinBalanceAmountException(
            BigDecimal.valueOf(1_000),
        )

        assertThrows<BelowMinBalanceAmountException> {
            service.use(command)
        }

        verify(exactly = 0) {
            recordRepository.save(any())
        }
    }

    @Test
    @DisplayName("getOrNullByUerId - 유저 Id로 잔고를 조회해 반환한다")
    fun getOrNullByUserIdWhenExists() {
        val userId = UserMock.id()
        val balance = BalanceMock.balance(userId = userId)
        every { repository.findByUserId(userId) } returns balance

        val result = service.getOrNullByUserId(userId)

        assertAll(
            { assertThat(result).isNotNull() },
            { assertThat(result?.id).isEqualTo(balance.id()) },
            { assertThat(result?.userId).isEqualTo(balance.userId) },
            { assertThat(result?.amount).isEqualByComparingTo(balance.amount.value) },
            { assertThat(result?.createdAt).isEqualTo(balance.createdAt) },
            { assertThat(result?.updatedAt).isEqualTo(balance.updatedAt) }
        )
    }

    @Test
    @DisplayName("getOrNullByUerId - 해당 유저의 잔고가 없는 경우, null이 반환된다")
    fun getOrNullByUserIdWhenNotExists() {
        val userId = UserMock.id()
        every { repository.findByUserId(userId) } returns null

        val result = service.getOrNullByUserId(userId)

        assertThat(result).isNull()
    }

    @Test
    @DisplayName("get - Id로 잔고를 조회해 반환한다")
    fun get() {
        val balanceId = BalanceMock.id()
        val balance = BalanceMock.balance(id = balanceId)
        every { repository.findById(balanceId.value) } returns balance

        val result = service.get(balanceId.value)

        assertAll(
            { assertThat(result.id).isEqualTo(balance.id()) },
            { assertThat(result.userId).isEqualTo(balance.userId) },
            { assertThat(result.amount).isEqualByComparingTo(balance.amount.value) },
            { assertThat(result.createdAt).isEqualTo(balance.createdAt) },
            { assertThat(result.updatedAt).isEqualTo(balance.updatedAt) }
        )
    }

    @Test
    @DisplayName("get - Id로 잔고를 조회했을 때, 해당 잔고가 없는 경우 - NotFoundBalanceException")
    fun getNotExistsBalance() {
        val balanceId = IdMock.value()
        every { repository.findById(balanceId) } returns null

        shouldThrow<NotFoundBalanceException> {
            service.get(balanceId)
        }
    }
}
