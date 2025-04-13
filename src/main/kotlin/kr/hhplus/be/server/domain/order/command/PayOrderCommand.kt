package kr.hhplus.be.server.domain.order.command

import kr.hhplus.be.server.domain.payment.PaymentReceipt

data class PayOrderCommand (
    val receipt: PaymentReceipt,
)
