package kr.hhplus.be.server.infra.order

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.order.repository.OrderRepository
import org.springframework.stereotype.Repository

@Repository
class OrderRepositoryImpl : OrderRepository {
    override fun save(order: Order): OrderId {
        TODO("Not yet implemented")
    }

    override fun findById(orderId: Long): Order? {
        TODO("Not yet implemented")
    }
}
