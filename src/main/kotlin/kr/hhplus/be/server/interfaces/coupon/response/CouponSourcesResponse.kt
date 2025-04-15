package kr.hhplus.be.server.interfaces.coupon.response

import kr.hhplus.be.server.domain.coupon.CouponSourceQueryModel
import kr.hhplus.be.server.interfaces.common.ServerApiResponse
import java.math.BigDecimal
import java.time.Instant

class CouponSourcesResponse(
    val coupons: List<CouponSourceResponse>,
): ServerApiResponse {
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
                coupon: CouponSourceQueryModel
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

    companion object {
        fun from(
            couponSources: List<CouponSourceQueryModel>,
        ): CouponSourcesResponse =
            CouponSourcesResponse(
                coupons = couponSources.map {
                    CouponSourceResponse.from(it)
                }
            )
    }
}
