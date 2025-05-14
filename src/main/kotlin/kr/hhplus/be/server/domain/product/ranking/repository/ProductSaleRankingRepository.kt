package kr.hhplus.be.server.domain.product.ranking.repository

import kr.hhplus.be.server.domain.product.ranking.ProductSaleRankingEntry

interface ProductSaleRankingRepository {
    fun updateRanking(entry: ProductSaleRankingEntry)
}
