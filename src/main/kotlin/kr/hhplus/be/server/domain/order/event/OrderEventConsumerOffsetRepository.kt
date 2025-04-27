package kr.hhplus.be.server.domain.order.event

interface OrderEventConsumerOffsetRepository {
    fun save(offset: OrderEventConsumerOffset): OrderEventConsumerOffset

    fun find(id: OrderEventConsumerOffsetId): OrderEventConsumerOffset?
}
