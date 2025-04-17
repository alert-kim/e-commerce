package kr.hhplus.be.server.domain.order

data class OrderCompletedEvent(
    val orderId: OrderId,
)
