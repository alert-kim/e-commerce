package kr.hhplus.be.server.domain.payment

import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.user.UserId

data class PaymentQueryModel(
    val id: PaymentId,
    val userId: UserId,
    val orderId: OrderId,
    val amount: String,
    val createdAt: String,
) {
    companion object {
        fun from(payment: Payment) =
            PaymentQueryModel(
                id = payment.requireId(),
                userId = payment.userId,
                orderId = payment.orderId,
                amount = payment.amount.toString(),
                createdAt = payment.createdAt.toString(),
            )
    }
}
