package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.coupon.command.UseCouponCommand
import kr.hhplus.be.server.domain.coupon.result.CouponUsedResult
import org.springframework.stereotype.Service

@Service
class CouponService {
    fun use(command: UseCouponCommand): CouponUsedResult = TODO()
}
