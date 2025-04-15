package kr.hhplus.be.server.domain.coupon

import java.math.BigDecimal
import java.time.Instant

data class CouponSourceQueryModel(
    val id: CouponSourceId,
    val name: String,
    val status: CouponSourceStatus,
    val quantity: Int,
    val discountAmount: BigDecimal,
    val createdAt: Instant,
    val updatedAt: Instant,
)
