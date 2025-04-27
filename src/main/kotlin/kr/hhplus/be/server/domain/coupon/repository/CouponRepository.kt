package kr.hhplus.be.server.domain.coupon.repository

import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.CouponId
import kr.hhplus.be.server.domain.user.UserId

interface CouponRepository {
    fun save(coupon: Coupon): Coupon
    fun findById(id: Long): Coupon?
    fun findAllByUserIdAndUsedAtIsNull(userId: UserId): List<Coupon>
}
