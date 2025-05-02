package kr.hhplus.be.server.testutil.mock

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigDecimal
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.domain.balance.*
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

object BalanceMock {
    fun id(
        value: Long = IdMock.value(),
    ) = BalanceId(value)

    fun recordId(
        value: Long = IdMock.value(),
    ) = BalanceRecordId(value)

    fun amount() = BalanceAmount.of(
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
        id = id?.value,
        userId = userId,
        amount = BalanceAmount.of(amount),
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

    fun record(
        id: BalanceRecordId? = recordId(),
        balanceId: BalanceId = id(),
        type: BalanceTransactionType = BalanceTransactionType.CHARGE,
        amount: BigDecimal = amount().value,
        createdAt: Instant = Instant.now(),
    ): BalanceRecord = BalanceRecord(
        id = id?.value,
        balanceId = balanceId,
        type = type,
        amount = BalanceAmount.of(amount),
        createdAt = createdAt,
    )
}
