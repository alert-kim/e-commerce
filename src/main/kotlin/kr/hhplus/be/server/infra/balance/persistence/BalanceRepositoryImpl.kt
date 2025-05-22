package kr.hhplus.be.server.infra.balance.persistence

import kr.hhplus.be.server.domain.balance.Balance
import kr.hhplus.be.server.domain.balance.repository.BalanceRepository
import kr.hhplus.be.server.domain.user.UserId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class BalanceRepositoryImpl(
    private val jpaRepository: BalanceJpaRepository,
) : BalanceRepository {
    override fun findById(id: Long): Balance? =
        jpaRepository.findByIdOrNull(id)

    override fun findByUserId(userId: UserId): Balance? =
        jpaRepository.findByUserId(userId.value)

    override fun save(balance: Balance): Balance =
        jpaRepository.save(balance)

    override fun update(balance: Balance) {
        jpaRepository.save(balance)
    }
}
