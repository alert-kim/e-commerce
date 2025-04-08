package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.user.UserId
import org.springframework.stereotype.Service

@Service
class BalanceService {
    fun getOrNullByUerId(userId: UserId): BalanceQueryModel? = TODO()
}
