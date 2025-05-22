package kr.hhplus.be.server.domain.order.command

import kr.hhplus.be.server.domain.order.OrderView

data class SendOrderCompletedCommand(
    val order: OrderView,
)
