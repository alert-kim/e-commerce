package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.balance.command.ChargeBalanceCommand
import kr.hhplus.be.server.domain.balance.command.UseBalanceCommand
import kr.hhplus.be.server.domain.balance.exception.NotFoundBalanceException
import kr.hhplus.be.server.domain.balance.repository.BalanceRepository
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
            ?: createAndGet(command.userId)

        balance.charge(BalanceAmount(command.amount))
        repository.update(balance)

        return balance.requireId()
    }

    fun use(command: UseBalanceCommand): UsedBalanceAmount {
        val balance = repository.findByUserId(command.userId)
            ?: throw NotFoundBalanceException("by userId: ${command.userId}")

        val usedAmount = balance.use(BalanceAmount(command.amount))
        repository.save(balance)
        return usedAmount
    }

    private fun createAndGet(userId: UserId): Balance {
        val balance = Balance.new(userId)
        val id = repository.save(balance)
        return repository.findById(id.value) ?: throw NotFoundBalanceException("by id: $id")
    }
}
