package kr.hhplus.be.server.domain.payment

import kr.hhplus.be.server.domain.payment.command.PayCommand
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
        )
        val id = repository.save(payment)
        return PaymentView.from(id, payment)
    }
}
