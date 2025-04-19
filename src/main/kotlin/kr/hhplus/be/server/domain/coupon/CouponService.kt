package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.coupon.command.CreateCouponCommand
import kr.hhplus.be.server.domain.coupon.command.UseCouponCommand
import kr.hhplus.be.server.domain.coupon.exception.NotFoundCouponException
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository
import kr.hhplus.be.server.domain.coupon.result.UsedCoupon
import kr.hhplus.be.server.domain.user.UserId
import org.springframework.stereotype.Service

@Service
class CouponService(
    private val repository: CouponRepository,
) {
    fun create(command: CreateCouponCommand): CouponView {
        val coupon = Coupon.new(
            userId = command.userId,
            couponSourceId = command.issuedCoupon.couponSourceId,
            name = command.issuedCoupon.name,
            discountAmount = command.issuedCoupon.discountAmount,
            createdAt = command.issuedCoupon.createdAt,
        )

        val id = repository.save(coupon)
        return CouponView.from(coupon, id)
    }

    fun use(command: UseCouponCommand): UsedCoupon {
        val coupon = repository.findById(command.couponId)
            ?: throw NotFoundCouponException("by id: ${command.couponId}")

        val usedCoupon = coupon.use(command.userId)
        repository.save(coupon)

        return usedCoupon
    }

    fun getAllUnused(userId: UserId): List<CouponView> =
        repository.findAllByUserIdAndUsedAtIsNull(userId)
            .map { CouponView.from(it) }

}
