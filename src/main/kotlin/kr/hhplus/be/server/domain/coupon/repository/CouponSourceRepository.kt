package kr.hhplus.be.server.domain.coupon.repository

import kr.hhplus.be.server.domain.coupon.CouponSource
import kr.hhplus.be.server.domain.coupon.CouponSourceId
import kr.hhplus.be.server.domain.coupon.CouponSourceStatus

interface CouponSourceRepository {
    fun save(couponSource: CouponSource): CouponSource

    fun findById(id: Long): CouponSource?

    fun findAllByStatus(status: CouponSourceStatus): List<CouponSource>
}
