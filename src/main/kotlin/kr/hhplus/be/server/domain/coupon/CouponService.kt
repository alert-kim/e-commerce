package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.coupon.command.CreateCouponCommand
import kr.hhplus.be.server.domain.coupon.command.UseCouponCommand
import kr.hhplus.be.server.domain.coupon.exception.NotFoundCouponException
import kr.hhplus.be.server.domain.coupon.result.CouponUsedResult
import kr.hhplus.be.server.domain.coupon.result.CreateCouponResult
import kr.hhplus.be.server.domain.user.UserId
import org.springframework.stereotype.Service

@Service
class CouponService(
    private val repository: CouponRepository,
) {
    fun create(command: CreateCouponCommand): CreateCouponResult {
        val coupon = Coupon.new(
            userId = command.userId,
            couponSourceId = command.issuedCoupon.couponSourceId,
            name = command.issuedCoupon.name,
            discountAmount = command.issuedCoupon.discountAmount,
            createdAt = command.issuedCoupon.createdAt,
        )

        repository.save(coupon)
        return CreateCouponResult(coupon)
    }

    fun use(command: UseCouponCommand): CouponUsedResult {
        val coupon = repository.findById(command.couponId)
            ?: throw NotFoundCouponException("by id: ${command.couponId}")

        coupon.use(command.userId)
        repository.save(coupon)

        return CouponUsedResult(
            value = coupon,
        )
    }

    fun getAllUnused(userId: UserId): List<Coupon> =
        repository.findAllByUserIdAndUsedAtIsNull(userId)

}
