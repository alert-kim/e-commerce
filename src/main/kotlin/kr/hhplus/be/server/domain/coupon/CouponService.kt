package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.coupon.command.CancelCouponUseCommand
import kr.hhplus.be.server.domain.coupon.command.CreateCouponCommand
import kr.hhplus.be.server.domain.coupon.command.UseCouponCommand
import kr.hhplus.be.server.domain.coupon.exception.NotFoundCouponException
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository
import kr.hhplus.be.server.domain.coupon.result.UsedCoupon
import kr.hhplus.be.server.domain.user.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CouponService(
    private val repository: CouponRepository,
) {

    @Transactional
    fun create(command: CreateCouponCommand): CouponView {
        val coupon = Coupon.new(
            userId = command.userId,
            couponSourceId = command.issuedCoupon.couponSourceId,
            name = command.issuedCoupon.name,
            discountAmount = command.issuedCoupon.discountAmount,
            createdAt = command.issuedCoupon.createdAt,
        ).let { repository.save(it) }

        return CouponView.from(coupon)
    }

    @Transactional
    fun use(command: UseCouponCommand): UsedCoupon {
        val coupon = repository.findById(command.couponId)
            ?: throw NotFoundCouponException("by id: ${command.couponId}")

        val usedCoupon = coupon.use(command.userId)

        return usedCoupon
    }

    @Transactional
    fun cancelUse(command: CancelCouponUseCommand) {
        val coupon = repository.findById(command.couponId)
            ?: throw NotFoundCouponException("by id: ${command.couponId}")

        coupon.cancelUse()
    }

    fun getAllUnused(userId: UserId): List<CouponView> =
        repository.findAllByUserIdAndUsedAtIsNull(userId)
            .map { CouponView.from(it) }

}
