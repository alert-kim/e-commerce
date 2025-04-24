package kr.hhplus.be.server.domain.order.command

import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.product.result.PurchasableProduct
import kr.hhplus.be.server.domain.stock.result.AllocatedStock

data class PlaceStockCommand(
    val orderId: OrderId,
    val preparedProductForOrder: List<PreparedProductForOrder>,
) {
    data class PreparedProductForOrder(
        val product: PurchasableProduct,
        val stock: AllocatedStock,
    ) {
        val productId = product.id

        init {
            require(product.id == stock.productId)
        }
    }

    companion object {
        fun of(
            orderId: OrderId,
            products: List<PurchasableProduct>,
            stocks: List<AllocatedStock>,
        ): PlaceStockCommand {
            val stocksByProductId = stocks.associateBy { it.productId }
            val preparedProductsForOrder = products.map {
                val stock = stocksByProductId.getValue(it.id)
                PreparedProductForOrder(
                    product = it,
                    stock = stock,
                )
            }
            return PlaceStockCommand(
                orderId = orderId,
                preparedProductForOrder = preparedProductsForOrder,
            )
        }
    }
}
