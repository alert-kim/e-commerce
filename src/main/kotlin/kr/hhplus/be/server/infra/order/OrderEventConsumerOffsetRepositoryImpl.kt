package kr.hhplus.be.server.infra.order

import kr.hhplus.be.server.domain.order.event.OrderEventConsumerOffset
import kr.hhplus.be.server.domain.order.event.OrderEventConsumerOffsetId
import kr.hhplus.be.server.domain.order.event.OrderEventConsumerOffsetRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class OrderEventConsumerOffsetRepositoryImpl(
    private val jpaRepository: OrderEventConsumerOffsetJpaRepository
) : OrderEventConsumerOffsetRepository {

    override fun save(offset: OrderEventConsumerOffset): OrderEventConsumerOffset =
        jpaRepository.save(offset)

    override fun find(id: OrderEventConsumerOffsetId): OrderEventConsumerOffset? =
        jpaRepository.findByIdOrNull(id)
}
