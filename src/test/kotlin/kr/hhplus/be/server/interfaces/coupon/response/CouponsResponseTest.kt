package kr.hhplus.be.server.interfaces.coupon.response

import kr.hhplus.be.server.application.coupon.result.GetCouponFacadeResult
import kr.hhplus.be.server.testutil.mock.CouponMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CouponsResponseTest {
    @Test
    fun `쿠폰 목록 대한 응답 생성`() {
        val coupons = List(3) { CouponMock.view() }
        val result = GetCouponFacadeResult.List(coupons)

        val response = CouponsResponse.from(result)

        assertThat(response.coupons.size).isEqualTo(coupons.size)
        response.coupons.forEachIndexed { index, coupon ->
            assertThat(coupon.id).isEqualTo(coupons[index].id.value)
            assertThat(coupon.name).isEqualTo(coupons[index].name)
            assertThat(coupon.discountAmount).isEqualByComparingTo(coupons[index].discountAmount)
            assertThat(coupon.createdAt).isEqualTo(coupons[index].createdAt)
            assertThat(coupon.updatedAt).isEqualTo(coupons[index].updatedAt)
        }
    }

    @Test
    fun `빈 리스트인 경우 빈 리스트 응답`() {
        val result = GetCouponFacadeResult.List(emptyList())

        val response = CouponsResponse.from(result)

        assertThat(response.coupons).isEmpty()
    }
}
