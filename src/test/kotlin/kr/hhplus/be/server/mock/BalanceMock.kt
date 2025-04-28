package kr.hhplus.be.server.mock

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigDecimal
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.domain.balance.*
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
        records: List<BalanceRecord> = emptyList(),
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ): Balance = Balance(
        id = id,
        userId = userId,
        amount = amount,
        records = records,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    fun view(
        id: BalanceId = id(),
        userId: UserId = UserMock.id(),
        amount: BigDecimal = amount().value,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) = BalanceView(
        id = id,
        userId = userId,
        amount = amount,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
