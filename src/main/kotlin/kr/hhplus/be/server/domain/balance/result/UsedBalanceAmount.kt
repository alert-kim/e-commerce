package kr.hhplus.be.server.domain.balance.result

import kr.hhplus.be.server.domain.balance.BalanceAmount
import kr.hhplus.be.server.domain.balance.BalanceId
import java.math.BigDecimal

data class UsedBalanceAmount (
    val balanceId: BalanceId,
    val amount: BalanceAmount,
) {
    val value: BigDecimal = amount.value
}
