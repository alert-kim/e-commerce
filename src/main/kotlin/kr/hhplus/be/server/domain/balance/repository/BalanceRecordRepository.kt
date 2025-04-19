package kr.hhplus.be.server.domain.balance.repository

import kr.hhplus.be.server.domain.balance.BalanceRecord
import kr.hhplus.be.server.domain.balance.BalanceRecordId

interface BalanceRecordRepository {
    fun save(record: BalanceRecord): BalanceRecordId
}
