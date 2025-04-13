package kr.hhplus.be.server.domain.payment

import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

class Payment(
    val id: PaymentId? = null,
    val userId: UserId,
    val order: OrderId,
    val amount: BigDecimal,
    val createdAt: Instant,
    status: PaymentStatus,
    updatedAt: Instant,
) {
    var status: PaymentStatus = status
        private set

    var updatedAt: Instant = updatedAt
        private set
}
