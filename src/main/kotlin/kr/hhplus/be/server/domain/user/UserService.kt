package kr.hhplus.be.server.domain.user

import kr.hhplus.be.server.domain.user.exception.NotFoundUserException
import org.springframework.stereotype.Service

@Service
class UserService(
    private val repository: UserRepository,
) {
    fun get(id: Long): User = repository.findById(id)
        ?: throw NotFoundUserException("by id: $id")
}
