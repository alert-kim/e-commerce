package kr.hhplus.be.server.domain.user.dto

import kr.hhplus.be.server.domain.user.User
import kr.hhplus.be.server.domain.user.UserId
import java.time.Instant

data class UserQueryModel(
    val id: UserId,
    val name: String,
    val createdAt: Instant,
) {
    companion object {
        fun from(user: User) = UserQueryModel(
            id = user.requireId(),
            name = user.name,
            createdAt = user.createdAt,
        )
    }
}

