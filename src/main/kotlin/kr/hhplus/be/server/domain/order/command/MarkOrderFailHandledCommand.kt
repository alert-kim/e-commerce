package kr.hhplus.be.server.domain.order.command

import kr.hhplus.be.server.domain.order.OrderId

data class MarkOrderFailHandledCommand(
    val orderId: OrderId
)
