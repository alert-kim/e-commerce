package kr.hhplus.be.server.infra.order

import kr.hhplus.be.server.domain.order.event.OrderEvent
import kr.hhplus.be.server.domain.order.event.OrderEventId
import kr.hhplus.be.server.domain.order.repository.OrderEventRepository
import org.springframework.stereotype.Repository

@Repository
class OrderEventRepositoryImpl(
    private val jpaRepository: OrderEventJpaRepository
) : OrderEventRepository {
    override fun save(event: OrderEvent): OrderEvent =
        jpaRepository.save(event)

    override fun findAllByIdAsc(): List<OrderEvent> =
        jpaRepository.findAllByIdAsc()

    override fun findAllByIdGreaterThanOrderByIdAsc(id: OrderEventId): List<OrderEvent> =
        jpaRepository.findAllByIdGreaterThanOrderByIdAsc(id.value)
}
