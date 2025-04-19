package kr.hhplus.be.server.domain.product

import java.time.LocalDate

interface ProductDailySaleRepository {
    fun findByProductIdAndDate(
        productId: ProductId,
        date: LocalDate,
    ): ProductDailySale?

    fun findTopNProductsByQuantity(
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int,
    ): List<ProductDailySale>

    fun save(sale: ProductDailySale)

    fun update(sale: ProductDailySale)
}
