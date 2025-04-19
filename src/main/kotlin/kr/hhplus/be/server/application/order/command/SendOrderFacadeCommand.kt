package kr.hhplus.be.server.application.order.command

import kr.hhplus.be.server.domain.order.OrderSnapshot

data class SendOrderFacadeCommand(
    val orderSnapshot: OrderSnapshot,
)
