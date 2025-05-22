package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.domain.balance.repository.BalanceRecordRepository
import kr.hhplus.be.server.testutil.mock.BalanceMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class BalanceRecordRepositoryTest @Autowired constructor(
    private val repository: BalanceRecordRepository
) : RepositoryTest() {

    @Nested
    @DisplayName("저장")
    inner class Save {
        @Test
        @DisplayName("성공 시 정상 반환")
        fun success() {
            val record = BalanceMock.record(id = null)

            val saved = repository.save(record)

            assertThat(record.id()).isEqualTo(saved.id())
            assertThat(record.balanceId).isEqualTo(saved.balanceId)
            assertThat(record.amount.value).isEqualByComparingTo(saved.amount.value)
            assertThat(record.createdAt).isEqualTo(saved.createdAt)
        }
    }

    @Nested
    @DisplayName("조회")
    inner class Find {
        @Test
        @DisplayName("ID로 조회 가능")
        fun findById() {
            val saved = repository.save(BalanceMock.record(id = null))

            val record = repository.findById(saved.id().value)

            assertThat(record?.id()).isEqualTo(saved.id())
            assertThat(record?.balanceId).isEqualTo(saved.balanceId)
            assertThat(record?.amount?.value).isEqualByComparingTo(saved.amount.value)
            assertThat(record?.createdAt).isEqualTo(saved.createdAt)
        }
    }
}
