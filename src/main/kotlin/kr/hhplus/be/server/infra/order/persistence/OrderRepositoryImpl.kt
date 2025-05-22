package kr.hhplus.be.server.infra.order.persistence

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.repository.OrderRepository
import org.springframework.stereotype.Repository

@Repository
class OrderRepositoryImpl(
    private val jpaRepository: OrderJpaRepository
) : OrderRepository {
    override fun save(order: Order): Order =
        jpaRepository.save(order)

    override fun findById(orderId: Long): Order? =
        jpaRepository.findById(orderId).orElse(null)
}
