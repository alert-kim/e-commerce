package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

data class UsedCoupon(
    val id: CouponId,
    val userId: UserId,
    val discountAmount: BigDecimal,
    val usedAt: Instant,
) {
    fun calculateDiscountAmount(totalAmount: BigDecimal): BigDecimal =
        totalAmount.min(discountAmount)
}
