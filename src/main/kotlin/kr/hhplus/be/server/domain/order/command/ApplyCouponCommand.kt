package kr.hhplus.be.server.domain.order.command

import kr.hhplus.be.server.domain.coupon.result.UsedCoupon
import kr.hhplus.be.server.domain.order.OrderId

data class ApplyCouponCommand(
    val orderId: OrderId,
    val usedCoupon: UsedCoupon,
)
