package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.domain.coupon.CouponQueryModel
import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.CouponSourceQueryModel
import kr.hhplus.be.server.domain.coupon.CouponSourceService
import kr.hhplus.be.server.domain.user.UserService
import org.springframework.stereotype.Service

@Service
class CouponFacade(
    private val couponService: CouponService,
    private val couponSourceService: CouponSourceService,
    private val userService: UserService,
) {
    fun issueCoupon(
        couponSourceId: Long,
        userId: Long,
    ): CouponQueryModel {
        TODO()
    }

    fun getAllSourcesIssuable(): List<CouponSourceQueryModel> =
        couponSourceService.getAllIssuable().map { CouponSourceQueryModel.from(it) }

    fun getCoupons(userId: Long): List<CouponQueryModel> {
        val userId = userService.get(userId).requireId()
        return couponService.getAllUnused(userId).map { CouponQueryModel.from(it) }
    }
}
