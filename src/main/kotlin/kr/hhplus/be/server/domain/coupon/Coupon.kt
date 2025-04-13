package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.coupon.exception.RequiredCouponIdException
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

class Coupon(
    val id: CouponId?,
    val userId: UserId,
    val name: String,
    val couponSourceId: CouponSourceId,
    val discountKind: CouponDiscountKind,
    val maxDiscountAmount: BigDecimal,
    val discountAmount: BigDecimal?,
    val discountRate: BigDecimal?,
    val usableFrom: Instant,
    val usableTo: Instant,
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

}
