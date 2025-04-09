package kr.hhplus.be.server.application.balance

import kr.hhplus.be.server.application.balance.command.ChargeBalanceFacadeCommand
import kr.hhplus.be.server.domain.balance.BalanceRecordService
import kr.hhplus.be.server.domain.balance.BalanceService
import kr.hhplus.be.server.domain.balance.BalanceTransactionType
import kr.hhplus.be.server.domain.balance.command.ChargeBalanceCommand
import kr.hhplus.be.server.domain.balance.command.CreateBalanceRecord
import kr.hhplus.be.server.domain.balance.dto.BalanceQueryModel
import kr.hhplus.be.server.domain.user.UserId
import kr.hhplus.be.server.domain.user.UserService
import org.springframework.stereotype.Service

@Service
class BalanceFacade(
    private val balanceService: BalanceService,
    private val balanceRecordService: BalanceRecordService,
    private val userService: UserService,
) {
    fun charge(command: ChargeBalanceFacadeCommand): BalanceQueryModel {
        val user = userService.get(command.userId)
        val balanceId = balanceService.charge(
            ChargeBalanceCommand(
                userId = user.requireId(),
                amount = command.amount,
            )
        )
        balanceRecordService.record(
            CreateBalanceRecord(
                balanceId = balanceId,
                amount = command.amount,
                type = BalanceTransactionType.CHARGE,
            )
        )
        return balanceService.get(balanceId.value).let { BalanceQueryModel.from(it) }
    }

    fun getOrNullByUerId(userId: UserId): BalanceQueryModel? =
        balanceService.getOrNullByUerId(userId)?.let { BalanceQueryModel.from(it) }
}
