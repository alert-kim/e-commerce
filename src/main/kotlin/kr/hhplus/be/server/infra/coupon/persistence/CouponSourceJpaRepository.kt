package kr.hhplus.be.server.infra.coupon.persistence

import kr.hhplus.be.server.domain.coupon.CouponSource
import kr.hhplus.be.server.domain.coupon.CouponSourceStatus
import org.springframework.data.jpa.repository.JpaRepository

interface CouponSourceJpaRepository : JpaRepository<CouponSource, Long> {
    fun findAllByStatus(status: CouponSourceStatus): List<CouponSource>
}
