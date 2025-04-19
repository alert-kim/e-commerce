package kr.hhplus.be.server.infra.balance

import kr.hhplus.be.server.domain.balance.Balance
import kr.hhplus.be.server.domain.balance.BalanceId
import kr.hhplus.be.server.domain.balance.repository.BalanceRepository
import kr.hhplus.be.server.domain.user.UserId
import org.springframework.stereotype.Repository

@Repository
class BalanceRepositoryImpl: BalanceRepository {
    override fun findById(id: Long): Balance? {
        TODO("Not yet implemented")
    }

    override fun findByUserId(userId: UserId): Balance? {
        TODO("Not yet implemented")
    }

    override fun save(balance: Balance): BalanceId {
        TODO("Not yet implemented")
    }

    override fun update(balance: Balance) {
        TODO("Not yet implemented")
    }
}
