package kr.hhplus.be.server.domain.user

import kr.hhplus.be.server.domain.user.exception.NotFoundUserException
import kr.hhplus.be.server.domain.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val repository: UserRepository,
) {
    fun get(id: Long): UserView {
        val user = repository.findById(id)
            ?: throw NotFoundUserException("by id: $id")
        return UserView.from(user)
    }
}
