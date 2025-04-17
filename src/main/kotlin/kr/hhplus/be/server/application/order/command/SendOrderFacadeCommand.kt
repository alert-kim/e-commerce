package kr.hhplus.be.server.application.order.command

import kr.hhplus.be.server.domain.order.dto.OrderSnapshot

data class SendOrderFacadeCommand(
    val orderSnapshot: OrderSnapshot,
)
