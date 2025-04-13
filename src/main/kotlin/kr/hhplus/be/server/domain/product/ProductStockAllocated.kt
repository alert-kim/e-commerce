package kr.hhplus.be.server.domain.product

import java.math.BigDecimal

data class ProductStockAllocated(
    val productId: ProductId,
    val quantity: Int,
    val unitPrice: BigDecimal,
)
