package kr.hhplus.be.server.application.order.command

import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.user.UserId

data class CancelOrderPaymentProcessorCommand(
    val orderId: OrderId,
    val userId: UserId,
)
