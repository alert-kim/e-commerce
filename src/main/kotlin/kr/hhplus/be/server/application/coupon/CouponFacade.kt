package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.application.coupon.command.IssueCouponFacadeCommand
import kr.hhplus.be.server.domain.coupon.CouponQueryModel
import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.CouponSourceQueryModel
import kr.hhplus.be.server.domain.coupon.CouponSourceService
import kr.hhplus.be.server.domain.coupon.command.CreateCouponCommand
import kr.hhplus.be.server.domain.coupon.command.IssueCouponCommand
import kr.hhplus.be.server.domain.user.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CouponFacade(
    private val couponService: CouponService,
    private val couponSourceService: CouponSourceService,
    private val userService: UserService,
) {

    fun issueCoupon(
        command: IssueCouponFacadeCommand
    ): CouponQueryModel {
        val userId = userService.get(command.userId).requireId()
        val issuedCoupon = couponSourceService.issue(IssueCouponCommand(command.couponSourceId)).coupon
        return couponService.create(CreateCouponCommand(userId, issuedCoupon)).let { CouponQueryModel.from(it.coupon) }
    }

    fun getAllSourcesIssuable(): List<CouponSourceQueryModel> =
        couponSourceService.getAllIssuable().map { CouponSourceQueryModel.from(it) }

    fun getCoupons(userId: Long): List<CouponQueryModel> {
        val userId = userService.get(userId).requireId()
        return couponService.getAllUnused(userId).map { CouponQueryModel.from(it) }
    }
}
