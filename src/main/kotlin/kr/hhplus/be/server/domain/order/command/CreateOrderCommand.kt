package kr.hhplus.be.server.domain.order.command

import kr.hhplus.be.server.domain.user.UserId

data class CreateOrderCommand(
    val userId: UserId,
)
