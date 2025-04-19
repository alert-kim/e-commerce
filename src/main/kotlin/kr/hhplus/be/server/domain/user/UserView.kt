package kr.hhplus.be.server.domain.user

import java.time.Instant

data class UserView(
    val id: UserId,
    val name: String,
    val createdAt: Instant,
) {
    companion object {
        fun from(user: User) = UserView(
            id = user.requireId(),
            name = user.name,
            createdAt = user.createdAt,
        )
    }
}

