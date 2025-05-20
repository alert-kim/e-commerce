package kr.hhplus.be.server.domain.order.event

import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.order.OrderView
import java.time.Instant

sealed interface OrderEvent{
    val orderId: OrderId
    val type: OrderEventType
    val createdAt: Instant
}

data class OrderCompletedEvent(
    override val orderId: OrderId,
    val order: OrderView,
    override val createdAt: Instant
): OrderEvent {
    override val type: OrderEventType = OrderEventType.COMPLETED
}

data class OrderFailedEvent(
    override val orderId: OrderId,
    val order: OrderView,
    override val createdAt: Instant
): OrderEvent {
    override val type: OrderEventType = OrderEventType.FAILED
}
