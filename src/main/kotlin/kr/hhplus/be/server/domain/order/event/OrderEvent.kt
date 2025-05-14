package kr.hhplus.be.server.domain.order.event

import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.order.OrderProduct
import kr.hhplus.be.server.domain.order.OrderSnapshot
import java.time.Instant

sealed interface OrderEvent{
    val orderId: OrderId
    val type: OrderEventType
    val snapshot: OrderSnapshot
    val createdAt: Instant
}

data class OrderCompletedEvent(
    override val orderId: OrderId,
    override val snapshot: OrderSnapshot,
    override val createdAt: Instant
): OrderEvent {
    override val type: OrderEventType = OrderEventType.COMPLETED

    val completedAt: Instant by lazy {
        snapshot.updatedAt
    }

    val orderProducts: List<OrderSnapshot.OrderProductSnapshot> by lazy {
        snapshot.orderProducts
    }
}

data class OrderFailedEvent(
    override val orderId: OrderId,
    override val snapshot: OrderSnapshot,
    override val createdAt: Instant
): OrderEvent {
    override val type: OrderEventType = OrderEventType.FAILED
}
