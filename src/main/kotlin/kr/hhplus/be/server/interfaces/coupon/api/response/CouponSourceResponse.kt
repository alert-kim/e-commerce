package kr.hhplus.be.server.interfaces.coupon.api.response

import kr.hhplus.be.server.domain.coupon.CouponSourceView
import java.math.BigDecimal
import java.time.Instant

data class CouponSourceResponse(
    val id: Long,
    val name: String,
    val quantity: Int,
    val discountAmount: BigDecimal?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(
            coupon: CouponSourceView
        ): CouponSourceResponse =
            CouponSourceResponse(
                id = coupon.id.value,
                name = coupon.name,
                quantity = coupon.quantity,
                discountAmount = coupon.discountAmount,
                createdAt = coupon.createdAt,
                updatedAt = coupon.updatedAt,
            )
    }
}
