package kr.hhplus.be.server.interfaces.balance.api.response

import kr.hhplus.be.server.application.balance.result.BalanceChargeFacadeResult
import kr.hhplus.be.server.application.balance.result.GetBalanceFacadeResult
import kr.hhplus.be.server.interfaces.common.api.ServerApiResponse
import java.math.BigDecimal

data class BalanceResponse(
    val userId: Long,
    val balance: BigDecimal,
) : ServerApiResponse {
    companion object {
        fun of(result: GetBalanceFacadeResult): BalanceResponse =
            when (result) {
                is GetBalanceFacadeResult.Found -> BalanceResponse(
                    userId = result.value.userId.value,
                    balance = result.value.amount,
                )
                is GetBalanceFacadeResult.Empty -> BalanceResponse(
                    userId = result.userId.value,
                    balance = BigDecimal.ZERO,
                )
            }

        fun of(result: BalanceChargeFacadeResult): BalanceResponse =
            BalanceResponse(
                userId = result.balance.userId.value,
                balance = result.balance.amount,
            )
    }
}
