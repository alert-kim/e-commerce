package kr.hhplus.be.server.application.order.command

import kr.hhplus.be.server.domain.order.event.OrderEvent

data class ConsumeOrderEventsFacadeCommand(
    val consumerId: String,
    val events: List<OrderEvent>,
)
