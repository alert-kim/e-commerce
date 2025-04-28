package kr.hhplus.be.server.domain.payment

import kr.hhplus.be.server.domain.order.OrderId
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
    companion object {
        fun from(id: PaymentId, payment: Payment) =
            PaymentView(
                id = id,
                userId = payment.userId,
                orderId = payment.orderId,
                amount = payment.amount,
                createdAt = payment.createdAt,
            )
    }
}
