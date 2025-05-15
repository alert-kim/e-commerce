package kr.hhplus.be.server.application.order.command

import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal

data class PayOrderProcessorCommand(
    val orderId: OrderId,
    val userId: UserId,
    val totalAmount: BigDecimal,
)
