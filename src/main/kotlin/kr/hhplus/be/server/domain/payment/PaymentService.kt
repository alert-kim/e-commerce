package kr.hhplus.be.server.domain.payment

import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.payment.command.CancelPaymentCommand
import kr.hhplus.be.server.domain.payment.command.PayCommand
import kr.hhplus.be.server.domain.payment.exception.NotFoundPaymentException
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    private val repository: PaymentRepository,
) {
    @Transactional
    fun pay(command: PayCommand): PaymentView {
        val payment = Payment.new(
            orderId = command.orderId,
            userId = command.userId,
            amount = command.amount,
        ).let { repository.save(it) }

        return PaymentView.from(payment)
    }

    @Transactional
    fun cancelPay(command: CancelPaymentCommand) {
        val payment = repository.findById(command.paymentId.value)
            ?: throw NotFoundPaymentException("by id: ${command.paymentId.value}")

        payment.cancel()
    }

    fun getOrNullByOrderId(orderId: OrderId): PaymentView? =
        repository.findByOrderId(orderId)
            ?.let { PaymentView.from(it) }

}
