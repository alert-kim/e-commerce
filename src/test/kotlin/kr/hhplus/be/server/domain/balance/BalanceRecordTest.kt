package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.balance.BalanceRecord
import kr.hhplus.be.server.domain.balance.BalanceTransactionType
import kr.hhplus.be.server.mock.BalanceMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class BalanceRecordTest {

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
            { assertThat(record.id).isNull() },
            { assertThat(record.balanceId).isEqualTo(balanceId) },
            { assertThat(record.type).isEqualTo(type) },
            { assertThat(record.balance).isEqualTo(amount) },
            { assertThat(record.createdAt).isNotNull() },
        )
    }
}
