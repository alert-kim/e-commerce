package kr.hhplus.be.server.domain.order.event

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.io.Serializable

@Embeddable
data class OrderEventConsumerOffsetId(
    @Column(name = "consumer_id")
    val consumerId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", columnDefinition = "varchar(20)")
    val eventType: OrderEventType
) : Serializable
