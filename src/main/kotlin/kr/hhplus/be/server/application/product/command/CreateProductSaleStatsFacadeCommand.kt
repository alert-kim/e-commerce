package kr.hhplus.be.server.application.product.command

import kr.hhplus.be.server.domain.order.OrderView

data class CreateProductSaleStatsFacadeCommand(
    val completedOrder: OrderView,
)
