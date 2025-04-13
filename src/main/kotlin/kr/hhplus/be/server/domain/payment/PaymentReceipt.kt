package kr.hhplus.be.server.domain.payment

import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

data class PaymentReceipt (
    val orderId: OrderId,
    val userId: UserId,
    val amount: BigDecimal,
    val at: Instant,
)
