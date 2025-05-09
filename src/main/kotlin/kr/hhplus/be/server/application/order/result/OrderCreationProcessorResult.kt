package kr.hhplus.be.server.application.order.result

import kr.hhplus.be.server.domain.order.OrderId

data class OrderCreationProcessorResult(
    val orderId: OrderId,
)
