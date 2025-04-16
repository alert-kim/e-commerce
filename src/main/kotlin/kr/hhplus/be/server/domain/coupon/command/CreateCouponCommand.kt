package kr.hhplus.be.server.domain.coupon.command

import kr.hhplus.be.server.domain.coupon.result.IssuedCoupon
import kr.hhplus.be.server.domain.user.UserId

data class CreateCouponCommand(
    val userId: UserId,
    val issuedCoupon: IssuedCoupon,
)
