package kr.hhplus.be.server.domain.product.command

import java.time.LocalDate

data class RecordProductDailySalesCommand(
    val sales: List<ProductSale>,
) {
    data class ProductSale(
        val productId: Long,
        val localDate: LocalDate,
        val quantity: Int,
    )
}

