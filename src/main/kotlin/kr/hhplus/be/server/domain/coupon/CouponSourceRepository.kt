package kr.hhplus.be.server.domain.coupon

interface CouponSourceRepository {
    fun findAllByStatus(status: CouponSourceStatus): List<CouponSource>
}
