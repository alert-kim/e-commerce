package kr.hhplus.be.server.application.order.command

import kr.hhplus.be.server.domain.coupon.CouponId

data class CancelCouponUseProcessorCommand(
    val couponId: CouponId,
)
