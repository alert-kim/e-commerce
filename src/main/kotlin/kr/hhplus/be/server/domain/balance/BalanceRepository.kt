package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.user.UserId

interface BalanceRepository {
    fun findByUserId(userId: UserId): Balance?
}
