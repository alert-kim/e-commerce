package kr.hhplus.be.server.util

import kr.hhplus.be.server.domain.balance.repository.BalanceRepository
import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.CouponSource
import kr.hhplus.be.server.domain.coupon.CouponSourceId
import kr.hhplus.be.server.domain.coupon.CouponSourceStatus
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository
import kr.hhplus.be.server.domain.coupon.repository.CouponSourceRepository
import kr.hhplus.be.server.domain.user.TestUserRepository
import kr.hhplus.be.server.domain.user.UserId
import kr.hhplus.be.server.domain.user.UserRepositoryTestConfig
import kr.hhplus.be.server.mock.BalanceMock
import kr.hhplus.be.server.mock.CouponMock
import kr.hhplus.be.server.mock.UserMock
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Instant

@Component
@Import(UserRepositoryTestConfig::class)
class DatabaseTestHelper(
    private val testUserRepository: TestUserRepository,
    private val balanceRepository: BalanceRepository,
    private val couponRepository: CouponRepository,
    private val couponSourceRepository: CouponSourceRepository
) {
    fun savedUser() = testUserRepository.save(UserMock.user(id = null))

    fun savedBalance(
        userId: UserId,
        amount: BigDecimal = BalanceMock.amount().value,
    ) = balanceRepository.save(BalanceMock.balance(id = null, userId = userId, amount = amount))

    fun savedCouponSource(
        name: String = "테스트 쿠폰",
        discountAmount: BigDecimal = BigDecimal.valueOf(1000),
        initialQuantity: Int = 20,
        quantity: Int = 10,
        status: CouponSourceStatus = CouponSourceStatus.ACTIVE
    ): CouponSource {
        val source = CouponMock.source(
            id = null,
            name = name,
            discountAmount = discountAmount,
            initialQuantity = initialQuantity,
            quantity = quantity,
            status = status
        )
        return couponSourceRepository.save(source)
    }

    fun savedCoupon(
        userId: UserId = UserMock.id(),
        couponSourceId: CouponSourceId = CouponMock.sourceId(),
        name: String = "테스트 쿠폰",
        discountAmount: BigDecimal = 1000.toBigDecimal(),
        usedAt: Instant? = null
    ): Coupon {
        val now = Instant.now()
        val coupon = CouponMock.coupon(
            id = null,
            userId = userId,
            name = name,
            couponSourceId = couponSourceId,
            discountAmount = discountAmount,
            createdAt = now,
            usedAt = usedAt,
            updatedAt = now
        )
        return couponRepository.save(coupon)
    }
}
