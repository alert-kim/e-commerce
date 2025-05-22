package kr.hhplus.be.server.domain.product.ranking.repository

import kr.hhplus.be.server.domain.order.event.OrderCompletedEvent

data class UpdateProductSaleRankingCommand(
    val event: OrderCompletedEvent,
)
