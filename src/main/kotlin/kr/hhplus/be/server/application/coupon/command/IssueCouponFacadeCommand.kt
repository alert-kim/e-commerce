package kr.hhplus.be.server.application.coupon.command

import kr.hhplus.be.server.domain.user.UserId

data class IssueCouponFacadeCommand (
    val couponSourceId: Long,
    val userId: Long,
)
