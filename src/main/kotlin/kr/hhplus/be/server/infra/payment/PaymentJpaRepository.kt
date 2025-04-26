package kr.hhplus.be.server.infra.payment

import kr.hhplus.be.server.domain.payment.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.Repository

interface PaymentJpaRepository : JpaRepository<Payment, Long> 