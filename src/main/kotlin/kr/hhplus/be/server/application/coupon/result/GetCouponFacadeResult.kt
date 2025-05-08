package kr.hhplus.be.server.application.coupon.result

import kr.hhplus.be.server.domain.coupon.CouponView

sealed class GetCouponFacadeResult {
    data class List(val value: kotlin.collections.List<CouponView>) : GetCouponFacadeResult()
}
