package kr.hhplus.be.server.domain.balance

interface BalanceRecordRepository {
    fun save(record: BalanceRecord): BalanceRecordId
}
