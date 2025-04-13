package kr.hhplus.be.server.domain.payment.command

import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal

data class PayCommand(
    val userId: UserId,
    val orderId: OrderId,
    val amount: BigDecimal,
)
