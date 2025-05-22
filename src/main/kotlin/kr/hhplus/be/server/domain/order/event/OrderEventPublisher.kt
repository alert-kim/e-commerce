package kr.hhplus.be.server.domain.order.event

interface OrderEventPublisher {
    fun publish(event: OrderCreatedEvent)
    fun publish(event: OrderStockPlacedEvent)
    fun publish(event: OrderCouponAppliedEvent)
    fun publish(event: OrderCompletedEvent)
    fun publish(event: OrderFailedEvent)
    fun publish(event: OrderMarkedFailedHandledEvent)
}
