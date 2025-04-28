package kr.hhplus.be.server.application.order.result

import kr.hhplus.be.server.domain.order.OrderView
import kr.hhplus.be.server.domain.order.event.OrderEvent

sealed class OrderResult {
    data class Single(val value: OrderView) : OrderResult()
    data class Events(val value: List<OrderEvent>) : OrderResult()
} 