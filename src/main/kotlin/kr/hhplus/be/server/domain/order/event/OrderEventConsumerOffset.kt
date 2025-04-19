package kr.hhplus.be.server.domain.order.event

import java.time.Instant

data class OrderEventConsumerOffset(
    val consumerId: String,
    val eventType: OrderEventType,
    val value: OrderEventId,
    val createdAt: Instant,
    val updatedAt: Instant,
)
