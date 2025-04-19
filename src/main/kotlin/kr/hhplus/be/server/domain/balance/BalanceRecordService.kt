package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.balance.command.CreateBalanceRecord
import kr.hhplus.be.server.domain.balance.repository.BalanceRecordRepository
import org.springframework.stereotype.Service

@Service
class BalanceRecordService(
    private val repository: BalanceRecordRepository,
) {
    fun record(command: CreateBalanceRecord) {
        repository.save(
            BalanceRecord.new(
                balanceId = command.balanceId,
                type = command.type,
                amount = BalanceAmount(command.amount),
            )
        )
    }
}


