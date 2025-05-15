package kr.hhplus.be.server.domain.stock.command

import kr.hhplus.be.server.domain.product.ProductId

data class AllocateStockCommand(
    val productId: ProductId,
    val quantity: Int,
)
