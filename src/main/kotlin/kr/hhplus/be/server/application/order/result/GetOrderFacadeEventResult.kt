package kr.hhplus.be.server.application.order.result

import kr.hhplus.be.server.domain.order.event.OrderEvent

sealed class GetOrderFacadeEventResult {
    data class List(val value: kotlin.collections.List<OrderEvent>) : GetOrderFacadeEventResult()
} 
