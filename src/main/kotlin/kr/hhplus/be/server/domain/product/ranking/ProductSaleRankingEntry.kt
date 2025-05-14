package kr.hhplus.be.server.domain.product.ranking

import kr.hhplus.be.server.domain.product.ProductId
import java.time.LocalDate

data class ProductSaleRankingEntry(
    val date: LocalDate,
    val productId: ProductId,
    val quantity: Int,
    val orderCount: Int,
)
