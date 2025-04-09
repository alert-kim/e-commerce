package kr.hhplus.be.server.application.balance

import kr.hhplus.be.server.application.balance.command.ChargeBalanceFacadeCommand
import kr.hhplus.be.server.application.balance.result.ChargeBalanceResult
import kr.hhplus.be.server.domain.balance.BalanceService
import kr.hhplus.be.server.domain.balance.dto.BalanceQueryModel
import kr.hhplus.be.server.domain.user.UserId
import org.springframework.stereotype.Service

@Service
class BalanceFacade(
    private val balanceService: BalanceService,
) {
    fun charge(command: ChargeBalanceFacadeCommand): BalanceQueryModel {
        TODO()
    }

    fun getOrNullByUerId(userId: UserId): BalanceQueryModel? =
        balanceService.getOrNullByUerId(userId)?.let { BalanceQueryModel.from(it) }
}
