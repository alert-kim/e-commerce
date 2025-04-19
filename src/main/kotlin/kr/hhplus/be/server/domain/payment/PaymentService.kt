package kr.hhplus.be.server.domain.payment

import kr.hhplus.be.server.domain.payment.command.PayCommand
import kr.hhplus.be.server.domain.payment.result.PayResult
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val repository: PaymentRepository,
) {
    fun pay(command: PayCommand): PayResult {
        val payment = Payment.new(
            orderId = command.orderId,
            userId = command.userId,
            amount = command.amount,
        )
        repository.save(payment)
        return PayResult(PaymentQueryModel.from(payment))
    }
}
