package kr.hhplus.be.server.infra.order

import kr.hhplus.be.server.domain.order.event.OrderJpaEvent
import kr.hhplus.be.server.domain.order.event.OrderEventId
import kr.hhplus.be.server.domain.order.repository.OrderEventRepository
import org.springframework.stereotype.Repository

@Repository
class OrderEventRepositoryImpl(
    private val jpaRepository: OrderEventJpaRepository
) : OrderEventRepository {
    override fun save(event: OrderJpaEvent): OrderJpaEvent =
        jpaRepository.save(event)

    override fun findAllByIdAsc(): List<OrderJpaEvent> =
        jpaRepository.findAllByIdAsc()

    override fun findAllByIdGreaterThanOrderByIdAsc(id: OrderEventId): List<OrderJpaEvent> =
        jpaRepository.findAllByIdGreaterThanOrderByIdAsc(id.value)
}
