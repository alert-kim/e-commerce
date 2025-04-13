package kr.hhplus.be.server.mock

import kr.hhplus.be.server.domain.coupon.CouponId
import kr.hhplus.be.server.domain.user.UserId

object CouponMock {
    fun id(): CouponId = CouponId(IdMock.value())
}
