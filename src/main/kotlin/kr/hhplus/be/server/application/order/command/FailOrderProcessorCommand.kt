package kr.hhplus.be.server.application.order.command

import kr.hhplus.be.server.domain.order.OrderId

data class FailOrderProcessorCommand(
    val orderId: OrderId,
    val reason: String? = null,
)
