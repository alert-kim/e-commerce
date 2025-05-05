package kr.hhplus.be.server.domain.payment

import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.payment.exception.NotOwnedPaymentException
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

data class PaymentView(
    val id: PaymentId,
    val userId: UserId,
    val orderId: OrderId,
    val amount: BigDecimal,
    val createdAt: Instant,
) {
    fun checkUser(userId: UserId): PaymentView {
        if (this.userId != userId) {
            throw NotOwnedPaymentException(
                userId = userId,
                ownerId = this.userId,
                paymentId = this.id,
            )
        }
        return this
    }

    companion object {
        fun from(payment: Payment) =
            PaymentView(
                id = payment.id(),
                userId = payment.userId,
                orderId = payment.orderId,
                amount = payment.amount,
                createdAt = payment.createdAt,
            )
    }
}
