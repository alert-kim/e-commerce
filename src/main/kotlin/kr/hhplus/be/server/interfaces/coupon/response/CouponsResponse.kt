package kr.hhplus.be.server.interfaces.coupon.response

import kr.hhplus.be.server.domain.coupon.CouponDiscountKind
import java.math.BigDecimal
import java.time.Instant

class CouponsResponse(
    val coupons: List<CouponResponse>,
) {
    data class CouponResponse(
        val id: Long,
        val name: String,
        val type: CouponDiscountKind,
        val quantity: Int,
        val maxDiscountAmount: BigDecimal,
        val discountAmount: BigDecimal?,
        val discountRate: BigDecimal?,
        val usableFrom: Instant,
        val usableTo: Instant,
    )
}
