package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.coupon.exception.AlreadyUsedCouponException
import kr.hhplus.be.server.domain.coupon.exception.NotOwnedCouponException
import kr.hhplus.be.server.domain.coupon.exception.RequiredCouponIdException
import kr.hhplus.be.server.domain.coupon.result.UsedCoupon
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

class Coupon(
    val id: CouponId?,
    val userId: UserId,
    val name: String,
    val couponSourceId: CouponSourceId,
    val discountAmount: BigDecimal,
    val createdAt: Instant,
    usedAt: Instant?,
    updatedAt: Instant,
) {
    var usedAt: Instant? = usedAt
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun requireId(): CouponId =
        id ?: throw RequiredCouponIdException()

    fun use(userId: UserId): UsedCoupon {
        if (this.userId != userId) {
            throw NotOwnedCouponException(id = requireId(), ownerId = this.userId, userId = userId)
        }
        if (usedAt != null) {
            throw AlreadyUsedCouponException(id = requireId())
        }
        val usedAt = Instant.now()
        this.usedAt = usedAt
        return UsedCoupon(
            id = requireId(),
            userId = userId,
            discountAmount = discountAmount,
            usedAt = usedAt,
        )
    }

    fun calculateDiscountAmount(totalAmount: BigDecimal): BigDecimal =
        totalAmount.min(discountAmount)

    companion object {
        fun new(
            userId: UserId,
            couponSourceId: CouponSourceId,
            name: String,
            discountAmount: BigDecimal,
            createdAt: Instant,
        ): Coupon = Coupon(
            id = null,
            userId = userId,
            name = name,
            couponSourceId = couponSourceId,
            discountAmount = discountAmount,
            usedAt = null,
            createdAt = createdAt,
            updatedAt = createdAt,
        )
    }
}
