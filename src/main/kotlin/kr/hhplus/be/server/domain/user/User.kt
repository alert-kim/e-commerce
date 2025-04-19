package kr.hhplus.be.server.domain.user

import kr.hhplus.be.server.domain.user.exception.RequiredUserIdException
import java.time.Instant

class User(
    var id: UserId? = null,
    val name: String,
    val createdAt: Instant = Instant.now(),
) {
    fun requireId(): UserId {
        return id ?: throw RequiredUserIdException()
    }
}
