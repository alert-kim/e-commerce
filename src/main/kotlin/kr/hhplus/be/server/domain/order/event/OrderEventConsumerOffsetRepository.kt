package kr.hhplus.be.server.domain.order.event

interface OrderEventConsumerOffsetRepository {
    fun save(offset: OrderEventConsumerOffset): OrderEventConsumerOffset

    fun update(update: OrderEventConsumerOffset)

    fun find(consumerId: String): OrderEventConsumerOffset?
}
