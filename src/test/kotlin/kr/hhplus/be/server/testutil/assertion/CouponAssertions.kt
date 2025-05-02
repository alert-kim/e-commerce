package kr.hhplus.be.server.testutil.assertion

import kr.hhplus.be.server.domain.coupon.Coupon
import org.assertj.core.api.AbstractAssert

class CouponAssert(actual: Coupon?) : AbstractAssert<CouponAssert, Coupon>(actual, CouponAssert::class.java) {

    fun isEqualTo(expected: Coupon): CouponAssert {
        when {
            actual == null -> failWithMessage("Coupon is null")
            actual.id() != expected.id() -> failWithMessage("Coupon id is not equal to expected")
            actual.userId != expected.userId -> failWithMessage("Coupon userId is not equal to expected")
            actual.name != expected.name -> failWithMessage("Coupon name is not equal to expected")
            actual.couponSourceId != expected.couponSourceId -> failWithMessage("Coupon couponSourceId is not equal to expected")
            actual.discountAmount.compareTo(expected.discountAmount) != 0 -> failWithMessage("Coupon discountAmount is not equal to expected")
            actual.createdAt != expected.createdAt -> failWithMessage("Coupon createdAt is not equal to expected")
            actual.updatedAt != expected.updatedAt -> failWithMessage("Coupon updatedAt is not equal to expected")
            actual.usedAt != expected.usedAt -> failWithMessage("Coupon usedAt is not equal to expected")
        }
        return this
    }

    companion object {
        fun assertCoupon(actual: Coupon?): CouponAssert {
            return CouponAssert(actual)
        }
    }
}
