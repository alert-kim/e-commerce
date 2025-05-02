package kr.hhplus.be.server.testutil.assertion

import kr.hhplus.be.server.domain.coupon.CouponSource
import org.assertj.core.api.AbstractAssert

class CouponSourceAssert(actual: CouponSource?) : AbstractAssert<CouponSourceAssert, CouponSource>(actual, CouponSourceAssert::class.java) {

    fun isEqualTo(expected: CouponSource): CouponSourceAssert {
        when {
            actual == null -> failWithMessage("CouponSource is null")
            actual.id() != expected.id() -> failWithMessage("CouponSource id is not equal to expected")
            actual.name != expected.name -> failWithMessage("CouponSource name is not equal to expected")
            actual.discountAmount.compareTo(expected.discountAmount) != 0 -> failWithMessage("CouponSource discountAmount is not equal to expected")
            actual.initialQuantity != expected.initialQuantity -> failWithMessage("CouponSource initialQuantity is not equal to expected")
            actual.quantity != expected.quantity -> failWithMessage("CouponSource quantity is not equal to expected")
            actual.status != expected.status -> failWithMessage("CouponSource status is not equal to expected")
            actual.createdAt != expected.createdAt -> failWithMessage("CouponSource createdAt is not equal to expected")
            actual.updatedAt != expected.updatedAt -> failWithMessage("CouponSource updatedAt is not equal to expected")
        }
        return this
    }

    companion object {
        fun assertCouponSource(actual: CouponSource?): CouponSourceAssert {
            return CouponSourceAssert(actual)
        }
    }
}
