package kr.hhplus.be.server.infra.payment

import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.payment.Payment
import kr.hhplus.be.server.domain.payment.PaymentId
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository
import org.springframework.stereotype.Repository

@Repository
class PaymentRepositoryImpl(
    private val jpaRepository: PaymentJpaRepository,
) : PaymentRepository {
    override fun save(payment: Payment): Payment =
        jpaRepository.save(payment)

    override fun findByOrderId(orderId: OrderId): Payment? =
        jpaRepository.findByOrderId(orderId)
}
