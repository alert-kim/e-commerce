package kr.hhplus.be.server.domain.order.command

import kr.hhplus.be.server.domain.order.event.OrderEvent

data class ConsumeOrderEventCommand(
    val consumerId: String,
    val event: OrderEvent,
)
