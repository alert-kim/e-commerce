package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.balance.dto.BalanceQueryModel
import kr.hhplus.be.server.domain.user.UserId
import org.springframework.stereotype.Service

@Service
class BalanceFacade(
    private val balanceService: BalanceService,
) {
    fun getOrNullByUerId(userId: UserId): BalanceQueryModel? =
        balanceService.getOrNullByUerId(userId)?.let { BalanceQueryModel.from(it) }
}
