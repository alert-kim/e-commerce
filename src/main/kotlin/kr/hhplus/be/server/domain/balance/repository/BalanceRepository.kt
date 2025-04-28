package kr.hhplus.be.server.domain.balance.repository

import kr.hhplus.be.server.domain.balance.Balance
import kr.hhplus.be.server.domain.balance.BalanceId
import kr.hhplus.be.server.domain.user.UserId

interface BalanceRepository {
    fun findById(id: Long): Balance?

    fun findByUserId(userId: UserId): Balance?

    fun save(balance: Balance): BalanceId

    fun update(balance: Balance)
}
