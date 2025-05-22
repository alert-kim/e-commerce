package kr.hhplus.be.server.infra.order.event

import kr.hhplus.be.server.domain.order.event.*
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class OrderEventPublisherImpl(
    private val publisher: ApplicationEventPublisher,
) : OrderEventPublisher {

    override fun publish(event: OrderCreatedEvent) {
        publisher.publishEvent(event)
    }

    override fun publish(event: OrderStockPlacedEvent) {
        publisher.publishEvent(event)
    }

    override fun publish(event: OrderCouponAppliedEvent) {
        publisher.publishEvent(event)
    }

    override fun publish(event: OrderCompletedEvent) {
        publisher.publishEvent(event)
    }

    override fun publish(event: OrderFailedEvent) {
        publisher.publishEvent(event)
    }

    override fun publish(event: OrderMarkedFailedHandledEvent) {
        publisher.publishEvent(event)
    }
}
