package kr.hhplus.be.server.domain.stock.result

import kr.hhplus.be.server.domain.product.ProductId

data class AllocatedStock(
    val productId: ProductId,
    val quantity: Int,
)
