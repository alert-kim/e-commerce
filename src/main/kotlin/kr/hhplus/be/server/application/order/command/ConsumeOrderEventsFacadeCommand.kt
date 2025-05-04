package kr.hhplus.be.server.application.order.command

import kr.hhplus.be.server.domain.order.event.OrderJpaEvent

data class ConsumeOrderEventsFacadeCommand(
    val consumerId: String,
    val events: List<OrderJpaEvent>,
)
