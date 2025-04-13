package kr.hhplus.be.server.domain.balance.command

import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal

data class UseBalanceCommand (
    val userId: UserId,
    val amount: BigDecimal,
)
