package kr.hhplus.be.server.domain.order.command

import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.order.OrderSheet

data class ApplyCouponCommand(
    val orderSheet: OrderSheet,
    val coupon: Coupon,
)
