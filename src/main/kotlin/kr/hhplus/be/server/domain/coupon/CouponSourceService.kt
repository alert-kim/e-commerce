package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.coupon.command.UseCouponCommand
import kr.hhplus.be.server.domain.coupon.exception.NotFoundCouponException
import kr.hhplus.be.server.domain.coupon.result.CouponUsedResult
import org.springframework.stereotype.Service

@Service
class CouponSourceService(
    private val repository: CouponRepository,
) {
    fun getAllIssuable(): List<CouponSource> {
        TODO()
    }
}
