package kr.hhplus.be.server.mock

import kr.hhplus.be.server.domain.balance.Balance
import kr.hhplus.be.server.domain.balance.BalanceId
import kr.hhplus.be.server.domain.balance.dto.BalanceQueryModel
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

object BalanceMock {
    fun id(): BalanceId = BalanceId(IdMock.value())

    fun balance(
        id: BalanceId? = id(),
        userId: UserId = UserMock.id(),
        amount: BigDecimal = BigDecimal(1_000),
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ): Balance = Balance(
        id = id,
        userId = userId,
        amount = amount,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    fun queryModel(
        id: BalanceId = id(),
        userId: UserId = UserMock.id(),
        amount: BigDecimal = BigDecimal(1_000),
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) = BalanceQueryModel(
        id = id,
        userId = userId,
        amount = amount,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
