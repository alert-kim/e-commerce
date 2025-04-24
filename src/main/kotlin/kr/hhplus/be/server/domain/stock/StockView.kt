package kr.hhplus.be.server.domain.stock

import kr.hhplus.be.server.domain.product.ProductId

data class StockView(
    val productId: ProductId,
    val quantity: Int,
)
