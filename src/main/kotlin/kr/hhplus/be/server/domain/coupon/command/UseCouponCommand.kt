package kr.hhplus.be.server.domain.coupon.command

import kr.hhplus.be.server.domain.user.UserId

data class UseCouponCommand (
    val couponId: Long,
    val userId: UserId,
)
