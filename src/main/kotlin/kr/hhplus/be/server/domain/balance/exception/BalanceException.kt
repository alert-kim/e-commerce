package kr.hhplus.be.server.domain.balance.exception

import kr.hhplus.be.server.domain.DomainException

abstract class BalanceException : DomainException()

class RequiredBalanceIdException : BalanceException() {
    override val message = "잔고 Id가 필요합니다"
}
