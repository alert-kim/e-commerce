package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.domain.balance.repository.BalanceRecordRepository
import kr.hhplus.be.server.testutil.mock.BalanceMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class BalanceRecordRepositoryTest: RepositoryTest() {
    @Autowired
    lateinit var repository: BalanceRecordRepository

    @Test
    fun `save - 반환된 BalanceRecord 필드 확인`() {
        val record = BalanceMock.record(id = null)

        val saved = repository.save(record)

        assertThat(record.id()).isEqualTo(saved.id())
        assertThat(record.balanceId).isEqualTo(saved.balanceId)
        assertThat(record.amount.value).isEqualByComparingTo(saved.amount.value)
        assertThat(record.createdAt).isEqualTo(saved.createdAt)
    }

    @Test
    fun `save & findById - 저장 후 id로 조회`() {
        val saved = repository.save(BalanceMock.record(id = null))

        val record = repository.findById(saved.id().value)

        assertThat(record?.id()).isEqualTo(saved.id())
        assertThat(record?.balanceId).isEqualTo(saved.balanceId)
        assertThat(record?.amount?.value).isEqualByComparingTo(saved.amount.value)
        assertThat(record?.createdAt).isEqualTo(saved.createdAt)
    }
}
