package kr.hhplus.be.server.infra.order

import kr.hhplus.be.server.domain.order.event.OrderEventConsumerOffset
import kr.hhplus.be.server.domain.order.event.OrderEventConsumerOffsetRepository
import kr.hhplus.be.server.domain.order.event.OrderEventType
import org.springframework.stereotype.Repository

@Repository
class OrderEventConsumerOffsetRepositoryImpl : OrderEventConsumerOffsetRepository {
    override fun save(offset: OrderEventConsumerOffset): OrderEventConsumerOffset {
        TODO("Not yet implemented")
    }

    override fun update(update: OrderEventConsumerOffset) {
        TODO("Not yet implemented")
    }

    override fun find(consumerId: String, eventType: OrderEventType): OrderEventConsumerOffset? {
        TODO("Not yet implemented")
    }
}
