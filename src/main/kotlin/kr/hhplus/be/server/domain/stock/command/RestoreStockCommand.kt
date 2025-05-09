package kr.hhplus.be.server.domain.stock.command

import kr.hhplus.be.server.domain.product.ProductId

data class RestoreStockCommand(
    val productId: ProductId,
    val quantity: Int
)
