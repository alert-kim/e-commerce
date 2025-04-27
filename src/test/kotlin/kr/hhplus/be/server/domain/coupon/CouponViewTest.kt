package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.mock.CouponMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class CouponViewTest {
    @Test
    fun `쿠폰 정보가 올바르게 변환된다`() {
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
