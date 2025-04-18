package kr.hhplus.be.server.application.order.command

import kr.hhplus.be.server.domain.order.event.OrderEvent

data class ConsumeOrderEventFacadeCommand(
    val consumerId: String,
    val event: OrderEvent,
)
