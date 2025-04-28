package kr.hhplus.be.server.domain.coupon.result

import kr.hhplus.be.server.domain.coupon.CouponSourceId
import java.math.BigDecimal
import java.time.Instant

data class IssuedCoupon(
    val couponSourceId: CouponSourceId,
    val name: String,
    val discountAmount: BigDecimal,
    val createdAt: Instant,
)
