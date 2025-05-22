package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.balance.exception.RequiredBalanceIdException
import kr.hhplus.be.server.testutil.mock.BalanceMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows

@DisplayName("잔고 레코드 테스트")
class BalanceRecordTest {

    @Nested
    @DisplayName("ID 조회")
    inner class Id {
        @Test
        @DisplayName("ID가 존재하면 반환")
        fun idExists() {
            val record = BalanceMock.record(id = BalanceMock.recordId())

            val result = record.id()

            assertThat(result).isEqualTo(record.id())
        }

        @Test
        @DisplayName("ID가 없으면 예외 발생")
        fun idNotExists() {
            val record = BalanceMock.record(id = null)

            assertThrows<RequiredBalanceIdException> {
                record.id()
            }
        }
    }

    @Nested
    @DisplayName("생성")
    inner class Create {
        @Test
        @DisplayName("정보로 잔고 레코드 생성")
        fun new() {
            val balanceId = BalanceMock.id()
            val type = BalanceTransactionType.entries.random()
            val amount = BalanceMock.amount()

            val record = BalanceRecord.new(
                balanceId = balanceId,
                type = type,
                amount = amount,
            )

            assertAll(
                { assertThat(record.balanceId).isEqualTo(balanceId) },
                { assertThat(record.type).isEqualTo(type) },
                { assertThat(record.amount).isEqualTo(amount) },
                { assertThat(record.createdAt).isNotNull() },
            )
        }
    }
}
