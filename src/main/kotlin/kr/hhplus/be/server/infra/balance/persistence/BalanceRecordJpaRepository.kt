package kr.hhplus.be.server.infra.balance.persistence

import kr.hhplus.be.server.domain.balance.BalanceRecord
import org.springframework.data.jpa.repository.JpaRepository

interface BalanceRecordJpaRepository : JpaRepository<BalanceRecord, Long>
