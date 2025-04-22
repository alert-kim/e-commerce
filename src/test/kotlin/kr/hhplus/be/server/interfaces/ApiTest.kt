package kr.hhplus.be.server.interfaces

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.domain.balance.repository.BalanceRepository
import kr.hhplus.be.server.domain.user.UserId
import kr.hhplus.be.server.domain.user.repository.UserRepository
import kr.hhplus.be.server.mock.BalanceMock
import kr.hhplus.be.server.util.DatabaseTestHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
abstract class ApiTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var databaseTestHelper: DatabaseTestHelper

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var balanceRepository: BalanceRepository

    fun savedUser() = databaseTestHelper.savedUser()

    fun savedBalance(
        userId: UserId,
        amount: BigDecimal = BalanceMock.amount().value,
    ) = databaseTestHelper.savedBalance(userId, amount)
}
