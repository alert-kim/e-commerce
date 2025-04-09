package kr.hhplus.be.server.application.user

import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.domain.user.dto.UserQueryModel
import org.springframework.stereotype.Service

@Service
class UserFacade(
    private val service: UserService,
) {
    fun get(id: Long): UserQueryModel = service.get(id)
        .let { UserQueryModel.from(it) }
}
