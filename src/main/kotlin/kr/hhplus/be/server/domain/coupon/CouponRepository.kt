package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.user.UserId

interface CouponRepository {
    fun save(coupon: Coupon)
    fun findById(id: Long): Coupon?
    fun findAllByUserIdAndUsedAtIsNull(userId: UserId): List<Coupon>
}
