package kr.hhplus.be.server.infra.user

import kr.hhplus.be.server.domain.user.User
import kr.hhplus.be.server.domain.user.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
    private val jpaRepository: UserJpaRepository,
) : UserRepository {
    override fun findById(id: Long): User? =
        jpaRepository.findByIdOrNull(id)
}
