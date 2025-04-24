package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.coupon.command.IssueCouponCommand
import kr.hhplus.be.server.domain.coupon.exception.NotFoundCouponSourceException
import kr.hhplus.be.server.domain.coupon.repository.CouponSourceRepository
import kr.hhplus.be.server.domain.coupon.result.IssuedCoupon
import org.springframework.stereotype.Service

@Service
class CouponSourceService(
    private val repository: CouponSourceRepository,
) {
    fun issue(command: IssueCouponCommand): IssuedCoupon {
        val couponSource = repository.findById(command.couponSourceId) ?: throw NotFoundCouponSourceException("by id: ${command.couponSourceId}")
        val issued = couponSource.issue()
        repository.save(couponSource)
        return issued
    }

    fun getAllIssuable(): List<CouponSourceView> =
        repository
            .findAllByStatus(CouponSourceStatus.ACTIVE)
            .map { CouponSourceView.from(it) }
}
