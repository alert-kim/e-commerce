package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.balance.command.ChargeBalanceCommand
import kr.hhplus.be.server.domain.balance.command.UseBalanceCommand
import kr.hhplus.be.server.domain.balance.exception.NotFoundBalanceException
import kr.hhplus.be.server.domain.balance.result.UsedBalanceAmount
import kr.hhplus.be.server.domain.user.UserId
import org.springframework.stereotype.Service

@Service
class BalanceService(
    private val repository: BalanceRepository
) {
    fun get(id: Long): Balance =
        repository.findById(id) ?: throw NotFoundBalanceException("by id: $id")

    fun getOrNullByUerId(userId: UserId): BalanceView? =
        repository.findByUserId(userId)
            ?.let { BalanceView.from(it) }

    fun charge(command: ChargeBalanceCommand): BalanceId {
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
        return balanceId
    }

    fun use(command: UseBalanceCommand): UsedBalanceAmount {
        val balance = repository.findByUserId(command.userId)
            ?: throw NotFoundBalanceException("by userId: ${command.userId}")

        val usedAmount = balance.use(BalanceAmount(command.amount))
        repository.save(balance)
        return usedAmount
    }
}
