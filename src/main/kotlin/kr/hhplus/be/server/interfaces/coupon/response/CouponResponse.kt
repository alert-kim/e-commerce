package kr.hhplus.be.server.interfaces.coupon.response

import kr.hhplus.be.server.domain.coupon.CouponView
import kr.hhplus.be.server.domain.user.UserId
import kr.hhplus.be.server.interfaces.common.ServerApiResponse
import java.math.BigDecimal
import java.time.Instant

data class CouponResponse(
    val id: Long,
    val userId: UserId,
    val name: String,
    val discountAmount: BigDecimal?,
    val usedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
): ServerApiResponse {
    companion object {
        fun from(coupon: CouponView) =
            CouponResponse(
                id = coupon.id.value,
                userId = coupon.userId,
                name = coupon.name,
                discountAmount = coupon.discountAmount,
                usedAt = coupon.usedAt,
                createdAt = coupon.createdAt,
                updatedAt = coupon.updatedAt,
            )
    }
}
