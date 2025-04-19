package kr.hhplus.be.server.domain.order.command

import kr.hhplus.be.server.domain.order.OrderSnapshot

data class SendOrderCompletedCommand(
    val orderSnapshot: OrderSnapshot,
)
