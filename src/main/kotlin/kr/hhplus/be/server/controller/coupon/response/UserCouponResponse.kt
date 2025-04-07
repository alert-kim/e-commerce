package kr.hhplus.be.server.controller.coupon.response

class UserCouponResponse(
    val id: Long,
    val userId: Long,
    val coupon: CouponsResponse.CouponResponse,
)
