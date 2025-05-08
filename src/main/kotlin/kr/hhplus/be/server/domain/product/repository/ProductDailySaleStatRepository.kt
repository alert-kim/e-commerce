package kr.hhplus.be.server.domain.product.repository

import kr.hhplus.be.server.domain.product.ProductDailySaleStat
import java.time.LocalDate

interface ProductDailySaleStatRepository {
    fun findTopNProductsByQuantity(
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int,
    ): List<ProductDailySaleStat>

    fun aggregateDailyStatsByDate(date: LocalDate)
}
