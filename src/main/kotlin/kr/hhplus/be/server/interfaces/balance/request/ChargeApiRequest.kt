package kr.hhplus.be.server.interfaces.balance.request

import java.math.BigDecimal

data class ChargeApiRequest(
    val userId: Long,
    val amount: BigDecimal,
)
