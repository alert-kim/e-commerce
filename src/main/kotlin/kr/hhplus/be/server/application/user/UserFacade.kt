package kr.hhplus.be.server.application.user

import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.domain.user.UserView
import org.springframework.stereotype.Service

@Service
class UserFacade(
    private val service: UserService,
) {
    fun get(id: Long): UserView = service.get(id)
}
