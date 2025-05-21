package kr.hhplus.be.server.domain.user

import kr.hhplus.be.server.infra.user.persistence.UserJpaRepository
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class UserRepositoryTestConfig {

    @Bean
    fun testUserRepository(userJpaRepository: UserJpaRepository): TestUserRepository =
        TestUserRepository(userJpaRepository)
}

class TestUserRepository(
    private val userJpaRepository: UserJpaRepository
) {
    fun save(user: User): User {
        return userJpaRepository.save(user)
    }
}
