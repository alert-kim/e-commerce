package kr.hhplus.be.server.domain.balance

import io.kotest.assertions.throwables.shouldThrow
import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.domain.balance.repository.BalanceRepository
import kr.hhplus.be.server.testutil.mock.BalanceMock
import kr.hhplus.be.server.testutil.mock.IdMock
import kr.hhplus.be.server.testutil.assertion.BalanceAssert.Companion.assertBalance
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException

class BalanceRepositoryTest @Autowired constructor(
    private val repository: BalanceRepository
) : RepositoryTest() {

    @Nested
    @DisplayName("저장")
    inner class Save {
        @Test
        @DisplayName("성공 시 정상 반환")
        fun success() {
            val balance = BalanceMock.balance(id = null)

            val saved = repository.save(balance)

            assertBalance(saved).isEqualTo(balance)
        }

        @Test
        @DisplayName("동일 유저ID로 중복 저장 시 예외 발생")
        fun duplicateUserId() {
            val userId = UserMock.id()
            repository.save(BalanceMock.balance(id = null, userId = userId))

            val sameUserIdBalance = BalanceMock.balance(id = null, userId = userId)
            shouldThrow<DataIntegrityViolationException> {
                repository.save(sameUserIdBalance)
            }
        }
    }

    @Nested
    @DisplayName("조회")
    inner class Find {
        @Test
        @DisplayName("ID로 조회 가능")
        fun findById() {
            val saved = repository.save(BalanceMock.balance(id = null))

            val result = repository.findById(saved.id().value)

            assertThat(result).isNotNull()
            assertBalance(result).isEqualTo(saved)
        }

        @Test
        @DisplayName("ID에 해당하는 잔고 없으면 null 반환")
        fun findByIdNotExists() {
            val result = repository.findById(IdMock.value())

            assertThat(result).isNull()
        }

        @Test
        @DisplayName("유저ID로 조회 가능")
        fun findByUserId() {
            val saved = repository.save(BalanceMock.balance(id = null))

            val result = repository.findByUserId(saved.userId)

            assertThat(result).isNotNull()
            assertBalance(result).isEqualTo(saved)
        }
    }
}
