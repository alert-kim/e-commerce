package kr.hhplus.be.server.domain.product.result

import kr.hhplus.be.server.domain.product.ProductId
import java.math.BigDecimal

data class ProductStockAllocated(
    val productId: ProductId,
    val quantity: Int,
    val unitPrice: BigDecimal,
)
