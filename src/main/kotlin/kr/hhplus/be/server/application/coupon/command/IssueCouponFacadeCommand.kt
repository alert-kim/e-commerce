package kr.hhplus.be.server.application.coupon.command

data class IssueCouponFacadeCommand (
    val couponSourceId: Long,
    val userId: Long,
)
