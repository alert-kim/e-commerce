package kr.hhplus.be.server.domain.product.command

import kr.hhplus.be.server.domain.product.ProductId
import java.time.LocalDate

data class RecordProductDailySalesCommand(
    val sales: List<ProductSale>,
) {
    data class ProductSale(
        val productId: ProductId,
        val date: LocalDate,
        val quantity: Int,
    )
}

