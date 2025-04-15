package kr.hhplus.be.server.mock

import kr.hhplus.be.server.domain.coupon.*
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

object CouponMock {
    fun id(): CouponId = CouponId(IdMock.value())

    fun sourceId(): CouponSourceId = CouponSourceId(IdMock.value())

    fun couponQueryModel(
        id: CouponId = id(),
        userId: UserId = UserMock.id(),
        name: String = "쿠폰",
        couponSourceId: CouponSourceId = sourceId(),
        discountAmount: BigDecimal = BigDecimal.valueOf(1_000),
        usedAt: Instant? = null,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) = CouponQueryModel(
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
        id = id,
        userId = userId,
        name = name,
        couponSourceId = couponSourceId,
        discountAmount = discountAmount,
        createdAt = createdAt,
        usedAt = usedAt,
        updatedAt = updatedAt,
    )

    fun sourceQueryModel(
        id: CouponSourceId = sourceId(),
        name: String = "쿠폰",
        status: CouponSourceStatus = CouponSourceStatus.ACTIVE,
        quantity: Int = 10,
        discountAmount: BigDecimal = BigDecimal.valueOf(1_000),
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ): CouponSourceQueryModel =
        CouponSourceQueryModel(
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
        discountAmount: BigDecimal = BigDecimal.valueOf(1_000),
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ): CouponSource =
        CouponSource(
            id = id,
            name = name,
            status = status,
            quantity = quantity,
            discountAmount = discountAmount,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
