package kr.hhplus.be.server.mock

import kr.hhplus.be.server.domain.balance.BalanceAmount

import kr.hhplus.be.server.domain.balance.Balance
import kr.hhplus.be.server.domain.balance.BalanceId
import kr.hhplus.be.server.domain.balance.dto.BalanceQueryModel
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigDecimal
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

object BalanceMock {
    fun id(): BalanceId = BalanceId(IdMock.value())

    fun amount() = BalanceAmount(
        value = Arb.bigDecimal(
            min = BalanceAmount.MIN_AMOUNT,
            max = BalanceAmount.MAX_AMOUNT,
        ).next(),
    )

    fun balance(
        id: BalanceId? = id(),
        userId: UserId = UserMock.id(),
        amount: BigDecimal = amount().value,
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
        amount: BigDecimal = amount().value,
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
