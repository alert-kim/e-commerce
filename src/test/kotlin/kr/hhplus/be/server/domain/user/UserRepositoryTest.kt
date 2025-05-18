package kr.hhplus.be.server.domain.user

import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.domain.user.repository.UserRepository
import kr.hhplus.be.server.testutil.mock.IdMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

@Import(UserRepositoryTestConfig::class)
class UserRepositoryTest @Autowired constructor(
    private val repository: UserRepository,
    private val testUserRepository: TestUserRepository
) : RepositoryTest() {

    @Nested
    @DisplayName("ID로 유저 조회")
    inner class FindById {

        @Test
        @DisplayName("해당 유저 반환")
        fun returnUser() {
            val user = UserMock.user(id = null)
            val saved = testUserRepository.save(user)

            val result = repository.findById(saved.id().value)

            assertThat(result).isNotNull()
            assertThat(result?.id()).isEqualTo(saved.id())
        }

        @Test
        @DisplayName("해당 유저가 없을 경우 null 반환")
        fun returnNull() {
            val result = repository.findById(IdMock.value())

            assertThat(result).isNull()
        }
    }
}
