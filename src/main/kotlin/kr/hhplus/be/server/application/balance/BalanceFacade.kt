package kr.hhplus.be.server.application.balance

import kr.hhplus.be.server.application.balance.command.ChargeBalanceFacadeCommand
import kr.hhplus.be.server.application.balance.result.BalanceChargeFacadeResult
import kr.hhplus.be.server.application.balance.result.GetBalanceFacadeResult
import kr.hhplus.be.server.domain.balance.BalanceService
import kr.hhplus.be.server.domain.balance.command.ChargeBalanceCommand
import kr.hhplus.be.server.domain.user.UserService
import org.springframework.stereotype.Service

@Service
class BalanceFacade(
    private val balanceService: BalanceService,
    private val userService: UserService,
) {
    fun charge(command: ChargeBalanceFacadeCommand): BalanceChargeFacadeResult {
        val user = userService.get(command.userId)
        val balanceId = balanceService.charge(
            ChargeBalanceCommand(
                userId = user.id,
                amount = command.amount,
            )
        )
        return BalanceChargeFacadeResult(balanceService.get(balanceId.value))
    }

    fun getOrNullByUerId(userId: Long): GetBalanceFacadeResult {
        val user = userService.get(userId)
        val balance = balanceService.getOrNullByUserId(user.id)
        return GetBalanceFacadeResult.of(user.id, balance)
    }
}
