package kr.hhplus.be.server.domain.order.event

import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.order.dto.OrderSnapshot
import java.time.Instant

class OrderEvent(
    val id: OrderEventId? = null,
    val orderId: OrderId,
    val type: OrderEventType,
    val snapshot: OrderSnapshot,
    val createdAt: Instant,
)
