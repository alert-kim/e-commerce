package kr.hhplus.be.server.interfaces.coupon.api.response

import kr.hhplus.be.server.application.coupon.result.GetCouponFacadeResult
import kr.hhplus.be.server.interfaces.common.api.ServerApiResponse

class CouponsResponse(
    val coupons: List<CouponResponse>,
) : ServerApiResponse {
    companion object {
        fun from(
            result: GetCouponFacadeResult.List,
        ): CouponsResponse =
            CouponsResponse(
                coupons = result.value.map {
                    CouponResponse.from(it)
                }
            )
    }
}
