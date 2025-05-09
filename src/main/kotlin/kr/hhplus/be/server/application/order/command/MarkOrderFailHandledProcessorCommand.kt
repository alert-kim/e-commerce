package kr.hhplus.be.server.application.order.command

import kr.hhplus.be.server.domain.order.OrderId

data class MarkOrderFailHandledProcessorCommand(
    val orderId: OrderId
)
