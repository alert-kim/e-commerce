package kr.hhplus.be.server.domain.product.ranking.repository

import kr.hhplus.be.server.domain.order.OrderView

data class UpdateProductSaleRankingCommand(
    val completedOrder: OrderView,
)
