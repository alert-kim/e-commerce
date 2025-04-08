package kr.hhplus.be.server.mock

import kr.hhplus.be.server.domain.user.UserId
import kr.hhplus.be.server.domain.user.UserQueryModel
import java.time.Instant

object UserMock {

    fun id(): UserId = UserId(IdMock.value())

    fun queryModel(
        id: UserId = id(),
        name: String = "홍길동",
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ): UserQueryModel = UserQueryModel(
        id = id,
        name = name,
        createdAt = createdAt,
        updateAt = updatedAt,
    )
}
