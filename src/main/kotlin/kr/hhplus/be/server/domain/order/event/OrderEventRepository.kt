package kr.hhplus.be.server.domain.order.event

interface OrderEventRepository {
    fun save(event: OrderEvent): OrderEventId

    fun findAllOrderByIdAsc(
    ): List<OrderEvent>

    fun findAllByIdGreaterThanOrderByIdAsc(
        id: OrderEventId,
    ): List<OrderEvent>
}
