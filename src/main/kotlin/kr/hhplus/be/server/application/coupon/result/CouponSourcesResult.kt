package kr.hhplus.be.server.application.coupon.result

import kr.hhplus.be.server.domain.coupon.CouponSourceView

data class CouponSourcesResult(
    val value: List<CouponSourceView>
)
