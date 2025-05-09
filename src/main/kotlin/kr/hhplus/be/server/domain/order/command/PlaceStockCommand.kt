package kr.hhplus.be.server.domain.order.command

import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.product.result.PurchasableProduct
import kr.hhplus.be.server.domain.stock.result.AllocatedStock

data class PlaceStockCommand(
    val orderId: OrderId,
    val product: PurchasableProduct,
    val stock: AllocatedStock,
)
