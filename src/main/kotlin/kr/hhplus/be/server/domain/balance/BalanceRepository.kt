package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.user.UserId

interface BalanceRepository {
    fun findById(id: Long): Balance?

    fun findByUserId(userId: UserId): Balance?

    fun save(balance: Balance): BalanceId
}
