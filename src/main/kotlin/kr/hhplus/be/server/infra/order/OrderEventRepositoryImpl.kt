package kr.hhplus.be.server.infra.order

import kr.hhplus.be.server.domain.order.event.OrderEvent
import kr.hhplus.be.server.domain.order.event.OrderEventId
import kr.hhplus.be.server.domain.order.repository.OrderEventRepository
import org.springframework.stereotype.Repository

@Repository
class OrderEventRepositoryImpl : OrderEventRepository {
    override fun save(event: OrderEvent): OrderEventId {
        TODO("Not yet implemented")
    }

    override fun findAllOrderByIdAsc(): List<OrderEvent> {
        TODO("Not yet implemented")
    }

    override fun findAllByIdGreaterThanOrderByIdAsc(id: OrderEventId): List<OrderEvent> {
        TODO("Not yet implemented")
    }
}
