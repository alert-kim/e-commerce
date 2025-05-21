package kr.hhplus.be.server.infra.payment.persistence

import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.payment.Payment
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository
import org.springframework.stereotype.Repository

@Repository
class PaymentRepositoryImpl(
    private val jpaRepository: PaymentJpaRepository,
) : PaymentRepository {
    override fun save(payment: Payment): Payment =
        jpaRepository.save(payment)

    override fun findById(id: Long): Payment? =
        jpaRepository.findById(id)
            .orElse(null)

    override fun findByOrderId(orderId: OrderId): Payment? =
        jpaRepository.findByOrderId(orderId)
}
