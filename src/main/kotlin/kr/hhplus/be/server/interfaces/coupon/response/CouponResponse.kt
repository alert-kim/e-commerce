package kr.hhplus.be.server.interfaces.coupon.response

import kr.hhplus.be.server.application.coupon.result.CouponResult
import kr.hhplus.be.server.domain.coupon.CouponView
import kr.hhplus.be.server.interfaces.common.ServerApiResponse
import java.math.BigDecimal
import java.time.Instant

class CouponResponse(
    val id: Long,
    val userId: Long,
    val name: String,
    val discountAmount: BigDecimal,
    val createdAt: Instant,
    val updatedAt: Instant,
) : ServerApiResponse {
    companion object {
        fun from(result: CouponResult.Single): CouponResponse {
            val coupon = result.value
            return CouponResponse(
                id = coupon.id.value,
                userId = coupon.userId.value,
                name = coupon.name,
                discountAmount = coupon.discountAmount,
                createdAt = coupon.createdAt,
                updatedAt = coupon.updatedAt,
            )
        }

        fun from(coupon: CouponView): CouponResponse {
            return CouponResponse(
                id = coupon.id.value,
                userId = coupon.userId.value,
                name = coupon.name,
                discountAmount = coupon.discountAmount,
                createdAt = coupon.createdAt,
                updatedAt = coupon.updatedAt,
            )
        }
    }
}
