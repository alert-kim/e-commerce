package kr.hhplus.be.server.domain.product.repository

import kr.hhplus.be.server.domain.product.ProductDailySale
import kr.hhplus.be.server.domain.product.ProductDailySaleId
import java.time.LocalDate

interface ProductDailySaleRepository {
    fun findById(id: ProductDailySaleId): ProductDailySale?

    fun findTopNProductsByQuantity(
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int,
    ): List<ProductDailySale>

    fun save(sale: ProductDailySale): ProductDailySale
}
