package kr.hhplus.be.server.domain.order.command

import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.product.result.ProductStockAllocated

data class PlaceStockCommand(
    val orderId: OrderId,
    val stocks: List<ProductStockAllocated>,
)
