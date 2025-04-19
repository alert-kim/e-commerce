package kr.hhplus.be.server.mock

import kr.hhplus.be.server.domain.user.User
import kr.hhplus.be.server.domain.user.UserId
import kr.hhplus.be.server.domain.user.UserView
import java.time.Instant

object UserMock {

    fun id(): UserId = UserId(IdMock.value())

    fun user(
        id: UserId? = id(),
        name: String = "홍길동",
        createdAt: Instant = Instant.now(),
    ) = User(
        id = id,
        name = name,
        createdAt = createdAt,
    )

    fun view(
        id: UserId = id(),
        name: String = "홍길동",
        createdAt: Instant = Instant.now(),
    ): UserView = UserView(
        id = id,
        name = name,
        createdAt = createdAt,
    )
}
