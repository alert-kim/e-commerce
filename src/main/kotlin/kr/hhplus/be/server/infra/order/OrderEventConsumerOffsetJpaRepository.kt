package kr.hhplus.be.server.infra.order

import kr.hhplus.be.server.domain.order.event.OrderEventConsumerOffset
import kr.hhplus.be.server.domain.order.event.OrderEventConsumerOffsetId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderEventConsumerOffsetJpaRepository : JpaRepository<OrderEventConsumerOffset, OrderEventConsumerOffsetId> {
}
