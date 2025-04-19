package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

data class CouponView(
    val id: CouponId,
    val userId: UserId,
    val name: String,
    val couponSourceId: CouponSourceId,
    val discountAmount: BigDecimal,
    val usedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(
            coupon: Coupon,
            id: CouponId? = null
        ): CouponView =
            CouponView(
                id = id ?: coupon.requireId(),
                userId = coupon.userId,
                name = coupon.name,
                couponSourceId = coupon.couponSourceId,
                discountAmount = coupon.discountAmount,
                usedAt = coupon.usedAt,
                createdAt = coupon.createdAt,
                updatedAt = coupon.updatedAt,
            )
    }
}
