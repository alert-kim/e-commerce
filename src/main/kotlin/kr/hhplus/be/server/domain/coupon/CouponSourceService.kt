package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.coupon.command.IssueCouponCommand
import kr.hhplus.be.server.domain.coupon.exception.NotFoundCouponSourceException
import kr.hhplus.be.server.domain.coupon.result.IssueCouponResult
import kr.hhplus.be.server.domain.coupon.result.IssuedCoupon
import org.springframework.stereotype.Service

@Service
class CouponSourceService(
    private val repository: CouponSourceRepository,
) {
    fun issue(command: IssueCouponCommand): IssueCouponResult {
        val couponSource = repository.findById(command.couponSourceId) ?: throw NotFoundCouponSourceException("by id: ${command.couponSourceId}")
        val issued = couponSource.issue()
        repository.save(couponSource)
        return IssueCouponResult(issued)
    }

    fun getAllIssuable(): List<CouponSource> =
        repository.findAllByStatus(CouponSourceStatus.ACTIVE)
}
