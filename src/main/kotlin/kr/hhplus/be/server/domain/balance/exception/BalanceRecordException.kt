package kr.hhplus.be.server.domain.balance.exception

import kr.hhplus.be.server.domain.DomainException
import java.math.BigDecimal

abstract class BalanceRecordException : DomainException()

class RequiredBalanceRecordIdException : BalanceRecordException() {
    override val message = "잔고 Id가 필요합니다"
}
