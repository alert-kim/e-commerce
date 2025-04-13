package kr.hhplus.be.server.domain.balance.exception

import kr.hhplus.be.server.domain.DomainException
import kr.hhplus.be.server.domain.balance.BalanceId
import java.math.BigDecimal

abstract class BalanceException : DomainException()

class RequiredBalanceIdException : BalanceException() {
    override val message = "잔고 Id가 필요합니다"
}

class ExceedMaxBalanceException(
    amount: BigDecimal,
) : BalanceException() {
    override val message: String = "잔고가 최대 금액을 초과했습니다. 현재 잔고: $amount"
}

class BelowMinBalanceException(
    amount: BigDecimal,
) : BalanceException() {
    override val message: String = "잔고가 최소 금액 미만입니다. 현재 잔고: $amount"
}

class NotFoundBalanceException(
    detail: String,
) : BalanceException() {
    override val message: String = "잔고를 찾을 수 없습니다. $detail"
}
