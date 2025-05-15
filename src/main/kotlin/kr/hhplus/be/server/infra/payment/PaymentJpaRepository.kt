package kr.hhplus.be.server.infra.payment

import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.payment.Payment
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentJpaRepository : JpaRepository<Payment, Long> {
    fun findByOrderId(orderId: OrderId): Payment?
}
