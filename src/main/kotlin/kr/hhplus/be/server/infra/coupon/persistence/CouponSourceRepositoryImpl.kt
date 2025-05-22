package kr.hhplus.be.server.infra.coupon.persistence

import kr.hhplus.be.server.domain.coupon.CouponSource
import kr.hhplus.be.server.domain.coupon.CouponSourceStatus
import kr.hhplus.be.server.domain.coupon.repository.CouponSourceRepository
import org.springframework.stereotype.Repository

@Repository
class CouponSourceRepositoryImpl(
    private val jpaRepository: CouponSourceJpaRepository
) : CouponSourceRepository {
    override fun save(couponSource: CouponSource): CouponSource =
        jpaRepository.save(couponSource)

    override fun findById(id: Long): CouponSource? =
        jpaRepository.findById(id).orElse(null)

    override fun findAllByStatus(status: CouponSourceStatus): List<CouponSource> =
        jpaRepository.findAllByStatus(status)
}
