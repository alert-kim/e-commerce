package kr.hhplus.be.server.interfaces.coupon.response

import kr.hhplus.be.server.application.coupon.result.IssueCouponFacadeResult
import kr.hhplus.be.server.testutil.mock.CouponMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CouponResponseTest {
    @Test
    fun `쿠폰에 대한 응답 생성 - IssueCouponFacadeResult`() {
        val coupon = CouponMock.view()
        val result = IssueCouponFacadeResult(coupon)

        val response = CouponResponse.from(result)

        assertThat(response.id).isEqualTo(coupon.id.value)
        assertThat(response.userId).isEqualTo(coupon.userId.value)
        assertThat(response.name).isEqualTo(coupon.name)
        assertThat(response.discountAmount).isEqualByComparingTo(coupon.discountAmount)
        assertThat(response.createdAt).isEqualTo(coupon.createdAt)
        assertThat(response.updatedAt).isEqualTo(coupon.updatedAt)
    }

    @Test
    fun `쿠폰에 대한 응답 생성 - CouponView`() {
        val coupon = CouponMock.view()

        val response = CouponResponse.from(coupon)

        assertThat(response.id).isEqualTo(coupon.id.value)
        assertThat(response.userId).isEqualTo(coupon.userId.value)
        assertThat(response.name).isEqualTo(coupon.name)
        assertThat(response.discountAmount).isEqualByComparingTo(coupon.discountAmount)
        assertThat(response.createdAt).isEqualTo(coupon.createdAt)
        assertThat(response.updatedAt).isEqualTo(coupon.updatedAt)
    }
}
