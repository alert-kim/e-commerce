package kr.hhplus.be.server.domain.order.repository

import kr.hhplus.be.server.domain.order.event.OrderJpaEvent
import kr.hhplus.be.server.domain.order.event.OrderEventId

interface OrderEventRepository {
    fun save(event: OrderJpaEvent): OrderJpaEvent

    fun findAllByIdAsc(
    ): List<OrderJpaEvent>

    fun findAllByIdGreaterThanOrderByIdAsc(
        id: OrderEventId,
    ): List<OrderJpaEvent>
}
