package kr.hhplus.be.server.application.balance.result

import kr.hhplus.be.server.domain.balance.BalanceView
import kr.hhplus.be.server.domain.user.UserId

sealed class BalanceResult {
    data class Found(
        val value: BalanceView,
    ) : BalanceResult()

    data class Empty(
        val userId: UserId,
    ) : BalanceResult()

    companion object {
        fun of(userId: UserId, balanceView: BalanceView?): BalanceResult =
           when (balanceView) {
                null -> Empty(userId)
                else -> Found(balanceView)
            }
    }
}
