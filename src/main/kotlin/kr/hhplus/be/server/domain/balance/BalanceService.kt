package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.balance.command.ChargeBalanceCommand
import kr.hhplus.be.server.domain.balance.command.UseBalanceCommand
import kr.hhplus.be.server.domain.balance.exception.NotFoundBalanceException
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

    fun charge(command: ChargeBalanceCommand): BalanceId {
        val balance = repository.findByUserId(command.userId)
        return when(balance) {
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
    }

    fun use(command: UseBalanceCommand): Balance {
        TODO()
    }
}
