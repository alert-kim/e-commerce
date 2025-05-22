package kr.hhplus.be.server.application.order.command

import kr.hhplus.be.server.domain.order.OrderView

data class CancelOrderFacadeCommand(
    val order: OrderView,
)
