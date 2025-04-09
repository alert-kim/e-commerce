package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.balance.exception.RequiredBalanceIdException
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

class Balance(
    val id: BalanceId? = null,
    val userId: UserId,
    var amount: BigDecimal,
    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now(),
) {
    fun requireId(): BalanceId =
        id ?: throw RequiredBalanceIdException()
}
