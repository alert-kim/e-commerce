package kr.hhplus.be.server.domain.payment

import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.payment.exception.RequiredPaymentIdException
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

class Payment(
    val id: PaymentId? = null,
    val userId: UserId,
    val orderId: OrderId,
    val amount: BigDecimal,
    val createdAt: Instant,
) {
    fun requireId(): PaymentId =
        id ?: throw RequiredPaymentIdException()

    companion object {
        fun new(
            userId: UserId,
            orderId: OrderId,
            amount: BigDecimal,
        ): Payment =
            Payment(
                userId = userId,
                orderId = orderId,
                amount = amount,
                createdAt = Instant.now(),
            )
    }
}
