package kr.hhplus.be.server.application.order.result

import kr.hhplus.be.server.domain.order.event.OrderJpaEvent

sealed class GetOrderFacadeEventResult {
    data class List(val value: kotlin.collections.List<OrderJpaEvent>) : GetOrderFacadeEventResult()
} 
