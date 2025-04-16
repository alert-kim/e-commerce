package kr.hhplus.be.server.domain.coupon

interface CouponSourceRepository {
    fun save(couponSource: CouponSource): CouponSourceId

    fun findById(id: Long): CouponSource?

    fun findAllByStatus(status: CouponSourceStatus): List<CouponSource>
}
