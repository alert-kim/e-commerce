package kr.hhplus.be.server.infra.payment

import kr.hhplus.be.server.domain.payment.Payment
import kr.hhplus.be.server.domain.payment.PaymentId
import kr.hhplus.be.server.domain.payment.PaymentRepository
import org.springframework.stereotype.Repository

@Repository
class PaymentRepositoryImpl : PaymentRepository {
    override fun save(payment: Payment): PaymentId {
        TODO("Not yet implemented")
    }
}
