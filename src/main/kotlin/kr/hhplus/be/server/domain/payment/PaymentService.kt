package kr.hhplus.be.server.domain.payment

import kr.hhplus.be.server.domain.payment.command.PayCommand
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val repository: PaymentRepository,
) {
    fun pay(command: PayCommand): PaymentView {
        val payment = Payment.new(
            orderId = command.orderId,
            userId = command.userId,
            amount = command.amount,
        ).let { repository.save(it) }

        return PaymentView.from(payment)
    }
}
