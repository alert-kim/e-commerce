package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.balance.command.ChargeBalanceCommand
import kr.hhplus.be.server.domain.balance.command.UseBalanceCommand
import kr.hhplus.be.server.domain.balance.exception.NotFoundBalanceException
import kr.hhplus.be.server.domain.balance.result.ChargeBalanceResult
import kr.hhplus.be.server.domain.user.UserId
import org.springframework.stereotype.Service

@Service
class BalanceService(
    private val repository: BalanceRepository
) {
    fun get(id: Long): Balance =
        repository.findById(id) ?: throw NotFoundBalanceException("by id: $id")

    fun getOrNullByUerId(userId: UserId): Balance? =
        repository.findByUserId(userId)

    fun charge(command: ChargeBalanceCommand): ChargeBalanceResult {
        val balance = repository.findByUserId(command.userId)
        val balanceId = when(balance) {
            null -> {
                val newBalance = Balance.new(command.userId)
                newBalance.charge(BalanceAmount(command.amount))
                repository.save(newBalance)
            }
            else -> {
                balance.charge(BalanceAmount(command.amount))
                repository.save(balance)
            }
        }
        return ChargeBalanceResult(
            balanceId = balanceId,
        )
    }

    fun use(command: UseBalanceCommand) {
        val balance = repository.findByUserId(command.userId)
            ?: throw NotFoundBalanceException("by userId: ${command.userId}")

        balance.use(BalanceAmount(command.amount))
        repository.save(balance)
    }
}
