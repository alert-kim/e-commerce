package kr.hhplus.be.server.domain.order.command

import kr.hhplus.be.server.domain.order.OrderId

data class FailOrderCommand(
    val orderId: OrderId,
    val reason: String
)
