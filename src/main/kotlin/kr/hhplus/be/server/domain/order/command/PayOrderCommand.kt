package kr.hhplus.be.server.domain.order.command

import kr.hhplus.be.server.domain.payment.PaymentQueryModel

data class PayOrderCommand(
    val payment: PaymentQueryModel,
)
