package kr.hhplus.be.server.application.balance.result

import kr.hhplus.be.server.domain.balance.BalanceView
import kr.hhplus.be.server.domain.user.UserId

sealed class GetBalanceFacadeResult {
    data class Found(
        val value: BalanceView,
    ) : GetBalanceFacadeResult()

    data class Empty(
        val userId: UserId,
    ) : GetBalanceFacadeResult()

    companion object {
        fun of(userId: UserId, balanceView: BalanceView?): GetBalanceFacadeResult =
           when (balanceView) {
                null -> Empty(userId)
                else -> Found(balanceView)
            }
    }
}
