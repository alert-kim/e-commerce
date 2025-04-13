package kr.hhplus.be.server.domain.order.command

import kr.hhplus.be.server.domain.payment.Payment

data class PayOrderCommand (
    val payment: Payment,
)
