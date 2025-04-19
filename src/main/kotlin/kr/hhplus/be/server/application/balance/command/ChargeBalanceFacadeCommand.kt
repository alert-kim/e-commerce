package kr.hhplus.be.server.application.balance.command

import java.math.BigDecimal

data class ChargeBalanceFacadeCommand(
    val userId: Long,
    val amount: BigDecimal,
)
