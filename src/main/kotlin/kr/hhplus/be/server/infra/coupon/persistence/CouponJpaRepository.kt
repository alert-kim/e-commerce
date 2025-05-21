package kr.hhplus.be.server.infra.coupon.persistence

import kr.hhplus.be.server.domain.coupon.Coupon
import org.springframework.data.jpa.repository.JpaRepository

interface CouponJpaRepository : JpaRepository<Coupon, Long> {
    fun findAllByUserIdAndUsedAtIsNull(userId: Long): List<Coupon>
}
