package kr.hhplus.be.server.application.balance

import kr.hhplus.be.server.application.balance.command.ChargeBalanceFacadeCommand
import kr.hhplus.be.server.application.balance.result.BalanceResult
import kr.hhplus.be.server.domain.balance.BalanceService
import kr.hhplus.be.server.domain.balance.command.ChargeBalanceCommand
import kr.hhplus.be.server.domain.user.UserId
import kr.hhplus.be.server.domain.user.UserService
import org.springframework.stereotype.Service

@Service
class BalanceFacade(
    private val balanceService: BalanceService,
    private val userService: UserService,
) {
    fun charge(command: ChargeBalanceFacadeCommand): BalanceResult.Found {
        val user = userService.get(command.userId)
        val balanceId = balanceService.charge(
            ChargeBalanceCommand(
                userId = user.id,
                amount = command.amount,
            )
        )
        return BalanceResult.Found(balanceService.get(balanceId.value))
    }

    fun getOrNullByUerId(userId: Long): BalanceResult {
        val user = userService.get(userId)
        val balance = balanceService.getOrNullByUserId(user.id)
        return BalanceResult.of(user.id, balance)
    }
}
