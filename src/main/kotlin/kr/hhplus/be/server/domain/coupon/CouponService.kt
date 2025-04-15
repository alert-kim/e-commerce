package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.coupon.command.UseCouponCommand
import kr.hhplus.be.server.domain.coupon.exception.NotFoundCouponException
import kr.hhplus.be.server.domain.coupon.result.CouponUsedResult
import kr.hhplus.be.server.domain.user.UserId
import org.springframework.stereotype.Service

@Service
class CouponService(
    private val repository: CouponRepository,
) {
    fun use(command: UseCouponCommand): CouponUsedResult {
        val coupon = repository.findById(command.couponId)
            ?: throw NotFoundCouponException("by id: ${command.couponId}")

        coupon.use(command.userId)
        repository.save(coupon)

        return CouponUsedResult(
            value = coupon,
        )
    }

    fun getAllUnused(userId: UserId): List<Coupon> {
        TODO()
    }
}
