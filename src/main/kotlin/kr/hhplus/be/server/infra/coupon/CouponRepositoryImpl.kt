package kr.hhplus.be.server.infra.coupon

import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.CouponId
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository
import kr.hhplus.be.server.domain.user.UserId
import org.springframework.stereotype.Repository

@Repository
class CouponRepositoryImpl: CouponRepository {
    override fun save(coupon: Coupon): CouponId {
        TODO("Not yet implemented")
    }

    override fun findById(id: Long): Coupon? {
        TODO("Not yet implemented")
    }

    override fun findAllByUserIdAndUsedAtIsNull(userId: UserId): List<Coupon> {
        TODO("Not yet implemented")
    }
}
