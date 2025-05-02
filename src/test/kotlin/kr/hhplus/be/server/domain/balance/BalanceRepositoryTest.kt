package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.domain.balance.repository.BalanceRepository
import kr.hhplus.be.server.testutil.mock.BalanceMock
import kr.hhplus.be.server.testutil.mock.IdMock
import kr.hhplus.be.server.testutil.assertion.BalanceAssert.Companion.assertBalance
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class BalanceRepositoryTest : RepositoryTest() {
    @Autowired
    lateinit var repository: BalanceRepository

    @Test
    fun `save - 반환된 Balance 필드 확인`() {
        val balance = BalanceMock.balance(id = null)

        val saved = repository.save(balance)

        assertBalance(saved).isEqualTo(balance)
    }

    @Test
    fun `findById - 잔고 조회`() {
        val saved = repository.save(BalanceMock.balance(id = null))

        val result = repository.findById(saved.id().value)

        assertThat(result).isNotNull()
        assertBalance(result).isEqualTo(saved)
    }

    @Test
    fun `findById - 잔고 없을 경우 null반환`() {
        val result = repository.findById(IdMock.value())

        assertThat(result).isNull()
    }

    @Test
    fun `findByUserId - 유저의 잔고 조회`() {
        val saved = repository.save(BalanceMock.balance(id = null))

        val result = repository.findByUserId(saved.userId)

        assertThat(result).isNotNull()
        assertBalance(result).isEqualTo(saved)
    }
}
