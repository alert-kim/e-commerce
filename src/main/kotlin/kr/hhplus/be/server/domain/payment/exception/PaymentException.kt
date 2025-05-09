package kr.hhplus.be.server.domain.payment.exception

import kr.hhplus.be.server.domain.DomainException
import kr.hhplus.be.server.domain.payment.PaymentId
import kr.hhplus.be.server.domain.user.UserId

abstract class PaymentException : DomainException()

class RequiredPaymentIdException : PaymentException() {
    override val message = "결제 ID가 필요합니다."
}

class NotOwnedPaymentException(
    val userId: UserId,
    val ownerId: UserId,
    val paymentId: PaymentId,
) : PaymentException() {
    override val message = "결제($paymentId) 소유자($ownerId)가 아닙니다. 요청자: $userId"
}

class NotFoundPaymentException(detail: String) : PaymentException() {
    override val message: String = "결제를 찾을 수 없습니다. $detail"
}
