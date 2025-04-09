package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.balance.dto.BalanceQueryModel
import kr.hhplus.be.server.domain.user.UserId
import org.springframework.stereotype.Service

@Service
class BalanceService(
    private val repository: BalanceRepository
) {
    fun get(id: Long): BalanceQueryModel = TODO()

    fun getOrNullByUerId(userId: UserId): Balance? =
        repository.findByUserId(userId)
}
