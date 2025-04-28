package kr.hhplus.be.server.domain.balance.repository

import kr.hhplus.be.server.domain.balance.BalanceRecord

interface BalanceRecordRepository {
    fun save(balance: BalanceRecord): BalanceRecord

    fun findById(id: Long): BalanceRecord?
}
