package kr.hhplus.be.server.application.product.command

import kr.hhplus.be.server.domain.order.OrderSnapshot

data class AggregateProductDailySalesFromOrderEventFacadeCommand(
    val sales: List<OrderSnapshot.OrderProductSnapshot>,
)
