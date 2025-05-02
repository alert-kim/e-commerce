package kr.hhplus.be.server.application.coupon.result

import kr.hhplus.be.server.domain.coupon.CouponSourceView

sealed class GetCouponSourcesFacadeResult {

    data class List(
        val value: kotlin.collections.List<CouponSourceView>
    ) : GetCouponSourcesFacadeResult()
}
