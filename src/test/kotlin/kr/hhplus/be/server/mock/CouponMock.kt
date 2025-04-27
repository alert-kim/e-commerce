package kr.hhplus.be.server.mock

import kr.hhplus.be.server.domain.coupon.*
import kr.hhplus.be.server.domain.coupon.result.IssuedCoupon
import kr.hhplus.be.server.domain.coupon.result.UsedCoupon
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

object CouponMock {
    fun id(): CouponId = CouponId(IdMock.value())

    fun sourceId(): CouponSourceId = CouponSourceId(IdMock.value())

    fun view(
        id: CouponId = id(),
        userId: UserId = UserMock.id(),
        name: String = "쿠폰",
        couponSourceId: CouponSourceId = sourceId(),
        discountAmount: BigDecimal = BigDecimal.valueOf(1_000),
        usedAt: Instant? = null,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) = CouponView(
        id = id,
        userId = userId,
        name = name,
        couponSourceId = couponSourceId,
        discountAmount = discountAmount,
        createdAt = createdAt,
        usedAt = usedAt,
        updatedAt = updatedAt,
    )

    fun coupon(
        id: CouponId? = id(),
        userId: UserId = UserMock.id(),
        name: String = "쿠폰",
        couponSourceId: CouponSourceId = sourceId(),
        discountAmount: BigDecimal = BigDecimal.valueOf(1_000),
        usedAt: Instant? = null,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) = Coupon(
        id = id?.value,
        userId = userId,
        name = name,
        couponSourceId = couponSourceId,
        discountAmount = discountAmount,
        createdAt = createdAt,
        usedAt = usedAt,
        updatedAt = updatedAt,
    )

    fun issuedCoupon(
        name: String = "쿠폰",
        couponSourceId: CouponSourceId = sourceId(),
        discountAmount: BigDecimal = BigDecimal.valueOf(1_000),
        createdAt: Instant = Instant.now(),
    ): IssuedCoupon =
        IssuedCoupon(
            name = name,
            couponSourceId = couponSourceId,
            discountAmount = discountAmount,
            createdAt = createdAt,
        )

    fun sourceView(
        id: CouponSourceId = sourceId(),
        name: String = "쿠폰",
        status: CouponSourceStatus = CouponSourceStatus.ACTIVE,
        quantity: Int = 10,
        discountAmount: BigDecimal = BigDecimal.valueOf(1_000),
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ): CouponSourceView =
        CouponSourceView(
            id = id,
            name = name,
            status = status,
            quantity = quantity,
            discountAmount = discountAmount,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun source(
        id: CouponSourceId? = sourceId(),
        name: String = "쿠폰",
        status: CouponSourceStatus = CouponSourceStatus.ACTIVE,
        quantity: Int = 10,
        initialQuantity: Int = 20,
        discountAmount: BigDecimal = BigDecimal.valueOf(1_000),
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ): CouponSource =
        CouponSource(
            id = id?.value,
            name = name,
            status = status,
            quantity = quantity,
            initialQuantity = initialQuantity,
            discountAmount = discountAmount,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun usedCoupon(
        id: CouponId = id(),
        userId: UserId = UserMock.id(),
        discountAmount: BigDecimal = BigDecimal.valueOf(1_000),
        usedAt: Instant = Instant.now(),
    ): UsedCoupon = UsedCoupon(
        id = id,
        userId = userId,
        discountAmount = discountAmount,
        usedAt = usedAt,
    )
}
