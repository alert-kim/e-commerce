package kr.hhplus.be.server.infra.user

import kr.hhplus.be.server.domain.user.User
import kr.hhplus.be.server.domain.user.repository.UserRepository
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl : UserRepository {
    override fun findById(id: Long): User? {
        TODO("Not yet implemented")
    }
}
