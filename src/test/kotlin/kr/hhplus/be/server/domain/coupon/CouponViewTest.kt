package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.testutil.mock.CouponMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class CouponViewTest {

    @Nested
    @DisplayName("변환")
    inner class Convert {
        @Test
        @DisplayName("쿠폰 정보가 올바르게 변환")
        fun success() {
            val coupon = CouponMock.coupon()

            val result = CouponView.from(coupon)

            assertAll(
                { assertThat(result.id).isEqualTo(coupon.id()) },
                { assertThat(result.userId).isEqualTo(coupon.userId) },
                { assertThat(result.name).isEqualTo(coupon.name) },
                { assertThat(result.couponSourceId).isEqualTo(coupon.couponSourceId) },
                { assertThat(result.discountAmount).isEqualByComparingTo(coupon.discountAmount) },
                { assertThat(result.usedAt).isEqualTo(coupon.usedAt) },
                { assertThat(result.createdAt).isEqualTo(coupon.createdAt) },
                { assertThat(result.updatedAt).isEqualTo(coupon.updatedAt) },
            )
        }
    }
}
