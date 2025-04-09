package kr.hhplus.be.server.domain.balance.command

import kr.hhplus.be.server.domain.balance.BalanceId
import kr.hhplus.be.server.domain.balance.BalanceTransactionType
import java.math.BigDecimal

data class CreateBalanceRecord(
    val balanceId: BalanceId,
    val type: BalanceTransactionType,
    val amount: BigDecimal,
)
