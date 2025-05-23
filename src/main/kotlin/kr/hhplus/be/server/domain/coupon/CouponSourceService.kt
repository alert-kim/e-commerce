package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.common.lock.LockStrategy
import kr.hhplus.be.server.common.lock.annotation.DistributedLock
import kr.hhplus.be.server.domain.coupon.command.IssueCouponCommand
import kr.hhplus.be.server.domain.coupon.exception.NotFoundCouponSourceException
import kr.hhplus.be.server.domain.coupon.repository.CouponSourceRepository
import kr.hhplus.be.server.domain.coupon.result.IssuedCoupon
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CouponSourceService(
    private val repository: CouponSourceRepository,
) {
    @DistributedLock(
        keyPrefix = "coupon-source",
        identifier = "#command.couponSourceId",
        strategy = LockStrategy.PUB_SUB,
        waitTime = 10_000L,
        leaseTime = 1_500L,
    )
    @Transactional
    fun issue(command: IssueCouponCommand): IssuedCoupon {
        val couponSource = repository.findById(command.couponSourceId) ?: throw NotFoundCouponSourceException("by id: ${command.couponSourceId}")
        val issued = couponSource.issue()
        return issued
    }

    fun getAllIssuable(): List<CouponSourceView> =
        repository
            .findAllByStatus(CouponSourceStatus.ACTIVE)
            .map { CouponSourceView.from(it) }
}
