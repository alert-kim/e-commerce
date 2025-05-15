package kr.hhplus.be.server.domain.payment.repository

import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.payment.Payment
import kr.hhplus.be.server.domain.payment.PaymentId
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository {
    fun save(payment: Payment): Payment
    fun findById(id: Long): Payment?
    fun findByOrderId(orderId: OrderId): Payment?
} 
