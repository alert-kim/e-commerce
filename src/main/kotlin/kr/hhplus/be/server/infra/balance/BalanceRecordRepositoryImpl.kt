package kr.hhplus.be.server.infra.balance

import kr.hhplus.be.server.domain.balance.BalanceRecord
import kr.hhplus.be.server.domain.balance.repository.BalanceRecordRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class BalanceRecordRepositoryImpl(
    private val jpaRepository: BalanceRecordJpaRepository
) : BalanceRecordRepository {
    override fun save(balance: BalanceRecord): BalanceRecord =
        jpaRepository.save(balance)

    override fun findById(id: Long): BalanceRecord? =
        jpaRepository.findByIdOrNull(id)
}
