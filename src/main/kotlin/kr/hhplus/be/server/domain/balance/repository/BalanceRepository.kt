package kr.hhplus.be.server.domain.balance.repository

import kr.hhplus.be.server.domain.balance.Balance
import kr.hhplus.be.server.domain.user.UserId

interface BalanceRepository {
    fun save(balance: Balance): Balance

    fun update(balance: Balance)

    fun findById(id: Long): Balance?

    fun findByUserId(userId: UserId): Balance?
}
