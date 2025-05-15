package kr.hhplus.be.server.infra.product.ranking

import kr.hhplus.be.server.domain.product.ranking.ProductSaleRankingEntry

internal object RedisProductSaleScoreCalculator {
    fun calculate(entry: ProductSaleRankingEntry): Double =
        entry.quantity + entry.orderCount.toDouble()
}
