package kr.hhplus.be.server.domain.balance

import io.kotest.assertions.throwables.shouldThrow
import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.domain.balance.repository.BalanceRepository
import kr.hhplus.be.server.domain.user.User
import kr.hhplus.be.server.domain.user.UserId
import kr.hhplus.be.server.testutil.mock.BalanceMock
import kr.hhplus.be.server.testutil.mock.IdMock
import kr.hhplus.be.server.testutil.assertion.BalanceAssert.Companion.assertBalance
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException

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
    fun `save - 같은 userId로 저장하면 유니크 제약조건 위반 예외 발생`() {
        val userId = UserMock.id()
        repository.save(BalanceMock.balance(id = null, userId = userId))

        val sameUserIdBalance = BalanceMock.balance(id = null, userId = userId)
        shouldThrow<DataIntegrityViolationException> {
            repository.save(sameUserIdBalance)
        }
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
