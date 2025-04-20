package kr.hhplus.be.server.infra.balance

import kr.hhplus.be.server.domain.balance.Balance
import org.springframework.data.jpa.repository.JpaRepository

interface BalanceJpaRepository : JpaRepository<Balance, Long> {
    fun findByUserId(userId: Long): Balance?
}
