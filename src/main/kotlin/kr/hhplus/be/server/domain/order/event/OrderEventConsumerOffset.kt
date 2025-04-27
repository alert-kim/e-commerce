package kr.hhplus.be.server.domain.order.event

import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "order_event_consumer_offsets")
class OrderEventConsumerOffset(
    @EmbeddedId
    val id: OrderEventConsumerOffsetId,

    val createdAt: Instant,
    eventId: OrderEventId,
    updatedAt: Instant,
) {

    @Column(name = "event_id", nullable = false)
    var eventId: OrderEventId = eventId
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun update(
        eventId: OrderEventId,
    ) {
        this.eventId = eventId
        this.updatedAt = Instant.now()
    }

    companion object {
        fun new(
            consumerId: String,
            eventId: OrderEventId,
            eventType: OrderEventType,
        ): OrderEventConsumerOffset = OrderEventConsumerOffset(
            id = OrderEventConsumerOffsetId(consumerId = consumerId, eventType = eventType),
            eventId = eventId,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
    }
}
