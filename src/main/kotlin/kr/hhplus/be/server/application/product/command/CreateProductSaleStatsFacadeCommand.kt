package kr.hhplus.be.server.application.product.command

import kr.hhplus.be.server.domain.order.event.OrderCompletedEvent

data class CreateProductSaleStatsFacadeCommand(
    val event: OrderCompletedEvent,
)
