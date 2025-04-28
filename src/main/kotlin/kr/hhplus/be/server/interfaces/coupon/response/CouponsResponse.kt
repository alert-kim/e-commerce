package kr.hhplus.be.server.interfaces.coupon.response

import kr.hhplus.be.server.application.coupon.result.CouponResult
import kr.hhplus.be.server.interfaces.common.ServerApiResponse

class CouponsResponse(
    val coupons: List<CouponResponse>,
) : ServerApiResponse {
    companion object {
        fun from(
            result: CouponResult.List,
        ): CouponsResponse =
            CouponsResponse(
                coupons = result.value.map {
                    CouponResponse.from(it)
                }
            )
    }
}
