package kr.hhplus.be.server.domain.payment.command

import kr.hhplus.be.server.domain.payment.PaymentId

data class CancelPaymentCommand(
    val paymentId: PaymentId,
)
