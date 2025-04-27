package kr.hhplus.be.server.interfaces

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.domain.balance.repository.BalanceRepository
import kr.hhplus.be.server.domain.coupon.CouponSourceId
import kr.hhplus.be.server.domain.coupon.CouponSourceStatus
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository
import kr.hhplus.be.server.domain.coupon.repository.CouponSourceRepository
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
import java.time.Instant

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
    lateinit var balanceRepository: BalanceRepository

    @Autowired
    lateinit var couponRepository: CouponRepository

    @Autowired
    lateinit var couponSourceRepository: CouponSourceRepository

    @Autowired
    lateinit var userRepository: UserRepository


    fun savedUser() = databaseTestHelper.savedUser()

    fun savedBalance(
        userId: UserId,
        amount: BigDecimal = BalanceMock.amount().value,
    ) = databaseTestHelper.savedBalance(userId, amount)

    fun savedCoupon(
        userId: UserId = savedUser().id(),
        usedAt: Instant? = null
    ) = databaseTestHelper.savedCoupon(
        userId = userId,
        usedAt = usedAt,
    )

    fun savedCouponSource(
        quantity: Int = 10,
        status: CouponSourceStatus = CouponSourceStatus.ACTIVE
    ) = databaseTestHelper.savedCouponSource(
        quantity = quantity,
        status = status
    )
}
