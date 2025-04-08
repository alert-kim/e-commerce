package kr.hhplus.be.server.mock

import kr.hhplus.be.server.domain.balance.BalanceId
import kr.hhplus.be.server.domain.balance.BalanceQueryModel
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

object BalanceMock {

    fun queryModel(
        id: BalanceId = BalanceId(IdMock.value()),
        userId: UserId = UserId(IdMock.value()),
        balance: BigDecimal = BigDecimal(1_000),
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) = BalanceQueryModel(
        id = id,
        userId = userId,
        amount = balance,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
