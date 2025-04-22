package kr.hhplus.be.server.util

import kr.hhplus.be.server.domain.balance.repository.BalanceRepository
import kr.hhplus.be.server.domain.user.TestUserRepository
import kr.hhplus.be.server.domain.user.UserId
import kr.hhplus.be.server.domain.user.UserRepositoryTestConfig
import kr.hhplus.be.server.domain.user.repository.UserRepository
import kr.hhplus.be.server.mock.BalanceMock
import kr.hhplus.be.server.mock.UserMock
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@Import(UserRepositoryTestConfig::class)
class DatabaseTestHelper(
    private val testUserRepository: TestUserRepository,
    private val balanceRepository: BalanceRepository,
) {
    fun savedUser() = testUserRepository.save(UserMock.user(id = null))

    fun savedBalance(
        userId: UserId,
        amount: BigDecimal = BalanceMock.amount().value,
    ) = balanceRepository.save(BalanceMock.balance(id = null, userId = userId, amount = amount))
}
