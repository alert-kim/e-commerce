package kr.hhplus.be.server.domain.product.repository

import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.product.stat.ProductDailySaleStat
import java.time.LocalDate

interface ProductDailySaleStatRepository {
    fun findTopNProductsByQuantity(
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int,
    ): List<ProductDailySaleStat>

    fun findTopNProductIdsByQuantity(
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int,
    ): List<ProductId>

    fun aggregateDailyStatsByDate(date: LocalDate)
}
