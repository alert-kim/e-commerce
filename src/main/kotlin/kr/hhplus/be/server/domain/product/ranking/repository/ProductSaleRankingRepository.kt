package kr.hhplus.be.server.domain.product.ranking.repository

import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.product.ranking.ProductSaleRankingEntry
import java.time.LocalDate

interface ProductSaleRankingRepository {
    fun updateRanking(entry: ProductSaleRankingEntry)
    fun renewRanking(startDate: LocalDate, endDate: LocalDate, limit: Int): List<ProductId>
    fun findTopNProductIds(startDate: LocalDate, endDate: LocalDate, limit: Int): List<ProductId>
}
