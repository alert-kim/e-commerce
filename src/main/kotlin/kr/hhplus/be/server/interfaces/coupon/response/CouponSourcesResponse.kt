package kr.hhplus.be.server.interfaces.coupon.response

import kr.hhplus.be.server.domain.coupon.CouponSourceView
import kr.hhplus.be.server.interfaces.common.ServerApiResponse

class CouponSourcesResponse(
    val coupons: List<CouponSourceResponse>,
) : ServerApiResponse {
    companion object {
        fun from(
            couponSources: List<CouponSourceView>,
        ): CouponSourcesResponse =
            CouponSourcesResponse(
                coupons = couponSources.map {
                    CouponSourceResponse.from(it)
                }
            )
    }
}
