package kr.hhplus.be.server.mock

import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.CouponDiscountKind
import kr.hhplus.be.server.domain.coupon.CouponId
import kr.hhplus.be.server.domain.coupon.CouponSourceId
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

object CouponMock {
    fun id(): CouponId = CouponId(IdMock.value())

    fun sourceId(): CouponSourceId = CouponSourceId(IdMock.value())

    fun coupon(
        id: CouponId? = id(),
        userId: UserId = UserMock.id(),
        name: String = "쿠폰",
        discountAmount: Long = 1_000,
        couponSourceId: CouponSourceId = sourceId(),
        discountKind: CouponDiscountKind = CouponDiscountKind.PERCENTAGE,
        maxDiscountAmount: BigDecimal = BigDecimal.valueOf(10_000),
        disCountAmount: BigDecimal = BigDecimal.valueOf(1_000),
        discountRate: BigDecimal? = null,
        usableFrom: Instant = Instant.now(),
        usableTo: Instant = Instant.now().plusSeconds(60 * 60 * 24),
        usedAt: Instant? = null,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) = Coupon(
        id = id,
        userId = userId,
        name = name,
        couponSourceId = couponSourceId,
        discountKind = discountKind,
        maxDiscountAmount = maxDiscountAmount,
        discountAmount = disCountAmount,
        discountRate = discountRate,
        usableFrom = usableFrom,
        usableTo = usableTo,
        createdAt = createdAt,
        usedAt = usedAt,
        updatedAt = updatedAt,
    )
}
