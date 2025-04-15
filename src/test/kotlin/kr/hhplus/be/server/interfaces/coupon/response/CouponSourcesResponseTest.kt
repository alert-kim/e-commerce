package kr.hhplus.be.server.interfaces.coupon.response

import kr.hhplus.be.server.mock.CouponMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CouponSourcesResponseTest {
    @Test
    fun `쿠폰 소스 대한 응답 생성`() {
        val couponSources = List(3) { CouponMock.sourceQueryModel() }

        val response = CouponSourcesResponse.from(couponSources)

        assertThat(response.coupons.size).isEqualTo(couponSources.size)
        response.coupons.forEachIndexed { index, source ->
            assertThat(source.id).isEqualTo(couponSources[index].id.value)
            assertThat(source.name).isEqualTo(couponSources[index].name)
            assertThat(source.quantity).isEqualTo(couponSources[index].quantity)
            assertThat(source.discountAmount).isEqualByComparingTo(couponSources[index].discountAmount)
            assertThat(source.createdAt).isEqualTo(couponSources[index].createdAt)
            assertThat(source.updatedAt).isEqualTo(couponSources[index].updatedAt)
        }
    }

    @Test
    fun `빈 리스트인 경우 빈 리스트 응답`() {

        val response = CouponSourcesResponse.from(emptyList())

        assertThat(response.coupons).isEmpty()
    }
}

