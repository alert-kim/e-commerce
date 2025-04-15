package kr.hhplus.be.server.interfaces.coupon.response

import kr.hhplus.be.server.domain.coupon.CouponQueryModel
import kr.hhplus.be.server.interfaces.common.ServerApiResponse

class CouponsResponse(
    val coupons: List<CouponResponse>
): ServerApiResponse {
    companion object {
        fun from(coupons: List<CouponQueryModel>): CouponsResponse =
            CouponsResponse(
                coupons = coupons.map { CouponResponse.from(it) }
            )
    }

}
