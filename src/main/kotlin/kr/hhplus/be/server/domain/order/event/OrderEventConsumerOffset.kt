package kr.hhplus.be.server.domain.order.event

import java.time.Instant

data class OrderEventConsumerOffset(
    val consumerId: String,
    val offset: OrderEventId,
    val eventType: OrderEventType,
    val createdAt: Instant,
    val updatedAt: Instant,
)
