package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.balance.exception.RequiredBalanceIdException
import kr.hhplus.be.server.testutil.mock.BalanceMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows

class BalanceRecordTest {

    @Test
    fun `requireId - id가 null이 아닌 경우 id 반환`() {
        val record = BalanceMock.record(id = BalanceMock.recordId())

        val result = record.id()

        assertThat(result).isEqualTo(record.id())
    }

    @Test
    fun `requireId - id가 null이면 RequiredBalanceIdException 발생`() {
        val record = BalanceMock.record(id = null)

        assertThrows<RequiredBalanceIdException> {
            record.id()
        }
    }

    @Test
    fun `new - 해당 정보로 잔고 레코드를 생성한다`() {
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
