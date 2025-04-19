package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.application.coupon.command.IssueCouponFacadeCommand
import kr.hhplus.be.server.domain.coupon.CouponView
import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.CouponSourceView
import kr.hhplus.be.server.domain.coupon.CouponSourceService
import kr.hhplus.be.server.domain.coupon.command.CreateCouponCommand
import kr.hhplus.be.server.domain.coupon.command.IssueCouponCommand
import kr.hhplus.be.server.domain.user.UserService
import org.springframework.stereotype.Service

@Service
class CouponFacade(
    private val couponService: CouponService,
    private val couponSourceService: CouponSourceService,
    private val userService: UserService,
) {

    fun issueCoupon(
        command: IssueCouponFacadeCommand
    ): CouponView {
        val userId = userService.get(command.userId).id
        val issuedCoupon = couponSourceService.issue(IssueCouponCommand(command.couponSourceId))
        return couponService.create(CreateCouponCommand(userId, issuedCoupon))
    }

    fun getAllSourcesIssuable(): List<CouponSourceView> =
        couponSourceService.getAllIssuable()

    fun getCoupons(userId: Long): List<CouponView> {
        val userId = userService.get(userId).id
        return couponService.getAllUnused(userId)
    }
}
