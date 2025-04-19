package kr.hhplus.be.server.application.product.command

import kr.hhplus.be.server.domain.order.OrderSnapshot

data class AggregateProductDailySalesFacadeCommand(
    val sales: List<OrderSnapshot.OrderProductSnapshot>,
)
