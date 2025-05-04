package kr.hhplus.be.server.domain.product.repository

import kr.hhplus.be.server.domain.product.ProductDailySale
import kr.hhplus.be.server.domain.product.ProductDailySaleId
import java.time.LocalDate

interface ProductDailySaleRepository {
    fun findTopNProductsByQuantity(
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int,
    ): List<ProductDailySale>

    fun aggregateDailyStatsByDate(date: LocalDate)
}
