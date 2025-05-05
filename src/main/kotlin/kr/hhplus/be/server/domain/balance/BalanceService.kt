package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.balance.command.CancelBalanceUseCommand
import kr.hhplus.be.server.domain.balance.command.ChargeBalanceCommand
import kr.hhplus.be.server.domain.balance.command.UseBalanceCommand
import kr.hhplus.be.server.domain.balance.exception.NotFoundBalanceException
import kr.hhplus.be.server.domain.balance.repository.BalanceRecordRepository
import kr.hhplus.be.server.domain.balance.repository.BalanceRepository
import kr.hhplus.be.server.domain.balance.result.UsedBalanceAmount
import kr.hhplus.be.server.domain.user.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BalanceService(
    private val repository: BalanceRepository,
    private val recordRepository: BalanceRecordRepository,
) {
    @Transactional
    fun charge(command: ChargeBalanceCommand): BalanceId {
        val balance = repository.findByUserId(command.userId)
            ?: createAndGet(command.userId)

        balance.charge(BalanceAmount.of(command.amount))

        recordRepository.save(
            BalanceRecord.new(
                balanceId = balance.id(),
                type = BalanceTransactionType.CHARGE,
                amount = BalanceAmount.of(command.amount),
            )
        )
        return balance.id()
    }

    @Transactional
    fun use(command: UseBalanceCommand): UsedBalanceAmount {
        val balance = repository.findByUserId(command.userId)
            ?: throw NotFoundBalanceException("by userId: ${command.userId}")

        val usedAmount = balance.use(BalanceAmount.of(command.amount))

        recordRepository.save(
            BalanceRecord.new(
                balanceId = balance.id(),
                type = BalanceTransactionType.USE,
                amount = BalanceAmount.of(command.amount),
            )
        )

        return usedAmount
    }

    @Transactional
    fun cancelUse(command: CancelBalanceUseCommand) {
        TODO()
    }

    fun get(id: Long): BalanceView =
        repository.findById(id)
            ?.let { BalanceView.from(it) }
            ?: throw NotFoundBalanceException("by id: $id")

    fun getOrNullByUserId(userId: UserId): BalanceView? =
        repository.findByUserId(userId)
            ?.let { BalanceView.from(it) }

    private fun createAndGet(userId: UserId): Balance {
        val balance = Balance.new(userId)
        return repository.save(balance)
    }
}
