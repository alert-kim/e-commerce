package kr.hhplus.be.server.interfaces.coupon.response

class UserCouponResponse(
    val id: Long,
    val userId: Long,
    val coupon: CouponsResponse.CouponResponse,
)
