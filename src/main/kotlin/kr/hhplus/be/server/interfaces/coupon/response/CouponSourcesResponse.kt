package kr.hhplus.be.server.interfaces.coupon.response

import kr.hhplus.be.server.application.coupon.result.CouponSourcesResult
import kr.hhplus.be.server.interfaces.common.ServerApiResponse

class CouponSourcesResponse(
    val coupons: List<CouponSourceResponse>,
) : ServerApiResponse {
    companion object {
        fun from(
            result: CouponSourcesResult,
        ): CouponSourcesResponse =
            CouponSourcesResponse(
                coupons = result.value.map {
                    CouponSourceResponse.from(it)
                }
            )
    }
}
