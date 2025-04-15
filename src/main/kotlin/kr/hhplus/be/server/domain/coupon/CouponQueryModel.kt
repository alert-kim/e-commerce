package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

data class CouponQueryModel(
    val id: CouponId,
    val userId: UserId,
    val name: String,
    val couponSourceId: CouponSourceId,
    val discountAmount: BigDecimal,
    val usedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
