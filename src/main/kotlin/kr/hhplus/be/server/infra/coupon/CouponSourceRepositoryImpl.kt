package kr.hhplus.be.server.infra.coupon

import kr.hhplus.be.server.domain.coupon.CouponSource
import kr.hhplus.be.server.domain.coupon.CouponSourceId
import kr.hhplus.be.server.domain.coupon.CouponSourceStatus
import kr.hhplus.be.server.domain.coupon.repository.CouponSourceRepository
import org.springframework.stereotype.Repository

@Repository
class CouponSourceRepositoryImpl : CouponSourceRepository {
    override fun save(couponSource: CouponSource): CouponSourceId {
        TODO("Not yet implemented")
    }

    override fun findById(id: Long): CouponSource? {
        TODO("Not yet implemented")
    }

    override fun findAllByStatus(status: CouponSourceStatus): List<CouponSource> {
        TODO("Not yet implemented")
    }
}
