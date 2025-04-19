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
            { assertThat(result.id).isEqualTo(coupon.id) },
            { assertThat(result.userId).isEqualTo(coupon.userId) },
            { assertThat(result.name).isEqualTo(coupon.name) },
            { assertThat(result.couponSourceId).isEqualTo(coupon.couponSourceId) },
            { assertThat(result.discountAmount).isEqualByComparingTo(coupon.discountAmount) },
            { assertThat(result.usedAt).isEqualTo(coupon.usedAt) },
            { assertThat(result.createdAt).isEqualTo(coupon.createdAt) },
            { assertThat(result.updatedAt).isEqualTo(coupon.updatedAt) },
        )
    }

    @Test
    fun `쿠폰 아이디가 따로 전달될 경우, id는 전달된 쿠폰 아이디다`() {
        val id = CouponMock.id()
        val coupon = CouponMock.coupon()

        val result = CouponView.from(coupon, id)

        assertThat(result.id).isEqualTo(id)
    }
}
