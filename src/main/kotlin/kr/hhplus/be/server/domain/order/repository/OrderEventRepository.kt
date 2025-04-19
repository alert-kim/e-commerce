package kr.hhplus.be.server.domain.order.repository

import kr.hhplus.be.server.domain.order.event.OrderEvent
import kr.hhplus.be.server.domain.order.event.OrderEventId

interface OrderEventRepository {
    fun save(event: OrderEvent): OrderEventId

    fun findAllOrderByIdAsc(
    ): List<OrderEvent>

    fun findAllByIdGreaterThanOrderByIdAsc(
        id: OrderEventId,
    ): List<OrderEvent>
}
