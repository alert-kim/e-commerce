package kr.hhplus.be.server.interfaces.coupon.response

import kr.hhplus.be.server.domain.coupon.CouponSourceQueryModel
import kr.hhplus.be.server.interfaces.common.ServerApiResponse

class CouponSourcesResponse(
    val coupons: List<CouponSourceResponse>,
) : ServerApiResponse {
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
