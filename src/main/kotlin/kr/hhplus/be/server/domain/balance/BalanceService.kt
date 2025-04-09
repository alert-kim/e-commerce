package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.balance.command.ChargeBalanceCommand
import kr.hhplus.be.server.domain.user.UserId
import org.springframework.stereotype.Service

@Service
class BalanceService(
    private val repository: BalanceRepository
) {
    fun get(id: Long): Balance = TODO()

    fun getOrNullByUerId(userId: UserId): Balance? =
        repository.findByUserId(userId)

    fun charge(command: ChargeBalanceCommand): BalanceId {
        TODO()
    }
}
