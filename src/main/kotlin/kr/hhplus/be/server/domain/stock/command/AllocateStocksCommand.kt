package kr.hhplus.be.server.domain.stock.command

import kr.hhplus.be.server.domain.product.ProductId

data class AllocateStocksCommand (
    val needStocks: Map<ProductId, Int>,
) {
    val productIds = needStocks.keys

    data class NeedStock(
        val productId: ProductId,
        val quantity: Int,
    )

    companion object {
        fun of(
            productId: ProductId,
            quantity: Int,
        ): NeedStock {
            return NeedStock(
                productId = productId,
                quantity = quantity,
            )
        }
    }
}
