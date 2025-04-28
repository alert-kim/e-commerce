package kr.hhplus.be.server.interfaces.balance.response

import kr.hhplus.be.server.application.balance.result.BalanceResult
import kr.hhplus.be.server.interfaces.common.ServerApiResponse
import java.math.BigDecimal

data class BalanceResponse(
    val userId: Long,
    val balance: BigDecimal,
) : ServerApiResponse {
    companion object {
        fun of(result: BalanceResult): BalanceResponse =
            when (result) {
                is BalanceResult.Found -> BalanceResponse(
                    userId = result.value.userId.value,
                    balance = result.value.amount,
                )
                is BalanceResult.Empty -> BalanceResponse(
                    userId = result.userId.value,
                    balance = BigDecimal.ZERO,
                )
            }
    }
}
