package kr.hhplus.be.server.application.coupon.result

import kr.hhplus.be.server.domain.coupon.CouponView

sealed class CouponResult {
    data class Single(val value: CouponView) : CouponResult()
    data class List(val value: kotlin.collections.List<CouponView>) : CouponResult()
}
