package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.coupon.command.IssueCouponCommand
import kr.hhplus.be.server.domain.coupon.result.IssueCouponResult
import kr.hhplus.be.server.domain.coupon.result.IssuedCoupon
import org.springframework.stereotype.Service

@Service
class CouponSourceService(
    private val repository: CouponSourceRepository,
) {
    fun issue(command: IssueCouponCommand): IssueCouponResult {
        TODO()
    }

    fun getAllIssuable(): List<CouponSource> =
        repository.findAllByStatus(CouponSourceStatus.ACTIVE)
}
