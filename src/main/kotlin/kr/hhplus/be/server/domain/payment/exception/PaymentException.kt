package kr.hhplus.be.server.domain.payment.exception

import kr.hhplus.be.server.domain.DomainException

abstract class PaymentException : DomainException()

class RequiredPaymentIdException : PaymentException() {
    override val message = "Payment Id is required."
}
