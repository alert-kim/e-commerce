package kr.hhplus.be.server.interfaces.coupon.response

import kr.hhplus.be.server.domain.coupon.CouponQueryModel
import java.math.BigDecimal
import java.time.Instant

data class CouponResponse(
    val id: Long,
    val userId: Long,
    val name: String,
    val discountAmount: BigDecimal?,
    val usedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(coupon: CouponQueryModel) =
            CouponResponse(
                id = coupon.id.value,
                userId = coupon.userId.value,
                name = coupon.name,
                discountAmount = coupon.discountAmount,
                usedAt = coupon.usedAt,
                createdAt = coupon.createdAt,
                updatedAt = coupon.updatedAt,
            )
    }
}
