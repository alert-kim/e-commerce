package kr.hhplus.be.server.domain.order.command

import kr.hhplus.be.server.domain.order.OrderSheet
import kr.hhplus.be.server.domain.product.ProductStockAllocated

data class PlaceStockCommand (
    val orderSheet: OrderSheet,
    val stocks: List<ProductStockAllocated>,
)
