package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.application.coupon.command.IssueCouponFacadeCommand
import kr.hhplus.be.server.application.coupon.result.CouponResult
import kr.hhplus.be.server.application.coupon.result.CouponSourcesResult
import kr.hhplus.be.server.domain.coupon.CouponService
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
    ): CouponResult.Single {
        val userId = userService.get(command.userId).id
        val issuedCoupon = couponSourceService.issue(IssueCouponCommand(command.couponSourceId))
        val couponView = couponService.create(CreateCouponCommand(userId, issuedCoupon))
        return CouponResult.Single(couponView)
    }

    fun getAllSourcesIssuable(): CouponSourcesResult =
        CouponSourcesResult(couponSourceService.getAllIssuable())

    fun getUsableCoupons(userId: Long): CouponResult.List {
        val userId = userService.get(userId).id
        val coupons = couponService.getAllUnused(userId)
        return CouponResult.List(coupons)
    }
}
