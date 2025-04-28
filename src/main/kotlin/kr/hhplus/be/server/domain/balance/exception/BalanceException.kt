package kr.hhplus.be.server.domain.balance.exception

import kr.hhplus.be.server.domain.DomainException
import kr.hhplus.be.server.domain.balance.BalanceId
import java.math.BigDecimal

abstract class BalanceException : DomainException()

class RequiredBalanceIdException : BalanceException() {
    override val message = "잔고 Id가 필요합니다"
}

class NotFoundBalanceException(
    detail: String,
) : BalanceException() {
    override val message: String = "잔고를 찾을 수 없습니다. $detail"
}

class ExceedMaxBalanceAmountException(
    amount: BigDecimal,
) : BalanceException() {
    override val message: String = "잔고의 최대 금액을 초과했습니다. $amount"
}

class BelowMinBalanceAmountException(
    amount: BigDecimal,
) : BalanceException() {
    override val message: String = "잔고의 최소 금액 미만입니다. $amount"
}

class InsufficientBalanceException(
    balanceId: BalanceId,
    amount: BigDecimal,
    required: BigDecimal,
) : BalanceException() {
    override val message: String = "잔고($balanceId)가 부족합니다. 필요 금액: $required, 현재 금액: $amount"
}
