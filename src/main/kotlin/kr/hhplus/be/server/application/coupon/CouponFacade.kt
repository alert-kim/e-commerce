package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.domain.coupon.CouponQueryModel
import kr.hhplus.be.server.domain.coupon.CouponSourceQueryModel
import kr.hhplus.be.server.domain.coupon.CouponSourceService
import kr.hhplus.be.server.domain.user.UserId
import org.springframework.stereotype.Service

@Service
class CouponFacade(
    private val couponSourceService: CouponSourceService,
) {
    fun getAllIssuable(): List<CouponSourceQueryModel> =
        couponSourceService.getAllIssuable().map { CouponSourceQueryModel.from(it) }

    fun getUserCoupons(userId: Long): List<CouponQueryModel> {
        TODO("Not yet implemented")
    }
}
