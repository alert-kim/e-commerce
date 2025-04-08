package kr.hhplus.be.server.domain.user

import java.time.Instant

data class UserQueryModel(
    val id: UserId,
    val name: String,
    val createdAt: Instant,
    val updateAt: Instant,
)

