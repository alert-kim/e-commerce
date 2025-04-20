package kr.hhplus.be.server.application.balance.command

import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal

data class ChargeBalanceFacadeCommand(
    val userId: Long,
    val amount: BigDecimal,
)
