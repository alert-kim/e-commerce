package kr.hhplus.be.server.domain.user

import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.domain.user.repository.UserRepository
import kr.hhplus.be.server.testutil.mock.IdMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

@Import(UserRepositoryTestConfig::class)
class UserRepositoryTest : RepositoryTest() {
    @Autowired
    lateinit var repository: UserRepository

    @Autowired
    lateinit var testUserRepository: TestUserRepository

    @Test
    fun `findById - 해당 유저 반환`() {
        val user = UserMock.user(id = null)
        val saved = testUserRepository.save(user)

        val result = repository.findById(saved.id().value)

        assertThat(result).isNotNull()
        assertThat(result?.id()).isEqualTo(saved.id())
    }

    @Test
    fun `findById - 해당 유저가 없을 경우 null반환`() {

        val result = repository.findById(IdMock.value())

        assertThat(result).isNull()
    }
}
