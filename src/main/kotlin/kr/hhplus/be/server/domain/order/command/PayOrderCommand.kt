package kr.hhplus.be.server.domain.order.command

import kr.hhplus.be.server.domain.payment.PaymentView

data class PayOrderCommand(
    val payment: PaymentView,
)
