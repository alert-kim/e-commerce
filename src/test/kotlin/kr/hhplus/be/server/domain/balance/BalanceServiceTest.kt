package kr.hhplus.be.server.domain.balance

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.balance.command.CancelBalanceUseCommand
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
import org.junit.jupiter.api.*
import java.math.BigDecimal

class BalanceServiceTest {
    private lateinit var service: BalanceService

    private val repository = mockk<BalanceRepository>(relaxed = true)
    private var recordRepository = mockk<BalanceRecordRepository>(relaxed = true)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        service = BalanceService(repository, recordRepository)
    }

    @Nested
    @DisplayName("잔액 충전")
    inner class Charge {
        @Test
        @DisplayName("잔고가 없으면 생성 후 충전하고 ID를 반환한다")
        fun create() {
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
        @DisplayName("기존 잔고가 있으면 충전 후 ID를 반환한다")
        fun update() {
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
        @DisplayName("충전 후 레코드를 생성한다")
        fun createRecord() {
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
        @DisplayName("충전 중 예외 발생 시 레코드를 생성하지 않는다")
        fun error() {
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
    }

    @Nested
    @DisplayName("잔액 사용")
    inner class Use {
        @Test
        @DisplayName("잔고를 사용하고 업데이트한다")
        fun update() {
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
        @DisplayName("사용 후 사용된 잔고 정보를 반환한다")
        fun result() {
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
        @DisplayName("사용 후 레코드를 생성한다")
        fun createRecord() {
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
        @DisplayName("잔고가 없으면 NotFoundBalanceException가 발생한다")
        fun notFound() {
            val userId = UserMock.id()
            val command = UseBalanceCommand(userId = userId, amount = BalanceMock.amount().value)
            every { repository.findByUserId(userId) } returns null

            shouldThrow<NotFoundBalanceException> {
                service.use(command)
            }
        }

        @Test
        @DisplayName("사용 중 예외 발생 시 레코드를 생성하지 않는다")
        fun error() {
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
    }

    @Nested
    @DisplayName("잔액 사용 취소")
    inner class CancelUse {
        @Test
        @DisplayName("잔액 사용을 취소한다")
        fun cancel() {
            val userId = UserMock.id()
            val balance = BalanceMock.balance(userId = userId)
            val balanceId = balance.id()
            val amount = 1000.toBigDecimal()
            every { repository.findByUserId(userId) } returns balance
            every { recordRepository.save(any()) } returns mockk()

            service.cancelUse(CancelBalanceUseCommand(userId, amount))

            verify {
                balance.cancelUse(BalanceAmount.of(amount))
                recordRepository.save(withArg {
                    assertThat(it.balanceId).isEqualTo(balanceId)
                    assertThat(it.type).isEqualTo(BalanceTransactionType.CANCEL_USE)
                    assertThat(it.amount.value).isEqualByComparingTo(amount)
                })
            }
        }

        @Test
        @DisplayName("잔액이 없으면 예외가 발생한다")
        fun notFound() {
            val userId = UserMock.id()
            val amount = 1000.toBigDecimal()
            every { repository.findByUserId(userId) } returns null

            assertThrows<NotFoundBalanceException> {
                service.cancelUse(CancelBalanceUseCommand(userId, amount))
            }
        }
    }

    @Nested
    @DisplayName("ID로 잔액 조회")
    inner class GetById {
        @Test
        @DisplayName("ID로 잔고를 조회한다")
        fun found() {
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
        @DisplayName("잔고가 없으면 예외가 발생한다")
        fun notFound() {
            val balanceId = IdMock.value()
            every { repository.findById(balanceId) } returns null

            shouldThrow<NotFoundBalanceException> {
                service.get(balanceId)
            }
        }
    }

    @Nested
    @DisplayName("사용자 ID로 잔액 조회")
    inner class GetByUserId {
        @Test
        @DisplayName("사용자 ID로 잔고 조회")
        fun found() {
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
        @DisplayName("잔고가 없으면 null을 반환")
        fun notFound() {
            val userId = UserMock.id()
            every { repository.findByUserId(userId) } returns null

            val result = service.getOrNullByUserId(userId)

            assertThat(result).isNull()
        }
    }
}
