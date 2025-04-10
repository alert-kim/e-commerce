package kr.hhplus.be.server.domain.balance

import io.mockk.clearMocks
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.domain.balance.command.CreateBalanceRecord
import kr.hhplus.be.server.mock.BalanceMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class BalanceRecordServiceTest {

    @MockK(relaxed = true)
    private lateinit var repository: BalanceRecordRepository

    @InjectMockKs
    private lateinit var service: BalanceRecordService

    @BeforeEach
    fun setup() {
        clearMocks(repository)
    }

    @Test
    fun `create - BalanceRecord가 생성된다`() {
        val command = CreateBalanceRecord(
            balanceId = BalanceMock.id(),
            type = BalanceTransactionType.entries.random(),
            amount = BalanceMock.amount().value,
        )

        service.record(command)

        verify {
            repository.save(withArg<BalanceRecord> {
                assertAll(
                    { assertThat(it.balanceId).isEqualTo(command.balanceId) },
                    { assertThat(it.type).isEqualTo(command.type) },
                    { assertThat(it.balance.value).isEqualByComparingTo(command.amount) },
                )
            })
        }
    }
}
