package kr.hhplus.be.server.infra.coupon

import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.CouponId
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository
import kr.hhplus.be.server.domain.user.UserId
import org.springframework.stereotype.Repository

@Repository
class CouponRepositoryImpl(
    private val jpaRepository: CouponJpaRepository
): CouponRepository {
    override fun save(coupon: Coupon): Coupon {
        return jpaRepository.save(coupon)
    }

    override fun findById(id: Long): Coupon? {
        return jpaRepository.findById(id).orElse(null)
    }

    override fun findAllByUserIdAndUsedAtIsNull(userId: UserId): List<Coupon> {
        return jpaRepository.findAllByUserIdAndUsedAtIsNull(userId.value)
    }
}
