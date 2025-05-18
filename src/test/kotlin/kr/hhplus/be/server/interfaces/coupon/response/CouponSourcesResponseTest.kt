package kr.hhplus.be.server.interfaces.coupon.response

import kr.hhplus.be.server.application.coupon.result.GetCouponSourcesFacadeResult
import kr.hhplus.be.server.testutil.mock.CouponMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import io.mockk.clearAllMocks

class CouponSourcesResponseTest {

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Nested
    @DisplayName("응답 생성")
    inner class From {

        @Test
        @DisplayName("쿠폰 소스 목록 응답 생성")
        fun fromList() {
            val couponSources = List(3) { CouponMock.sourceView() }
            val result = GetCouponSourcesFacadeResult.List(couponSources)

            val response = CouponSourcesResponse.from(result)

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
        @DisplayName("빈 리스트 응답 생성")
        fun fromEmptyList() {
            val result = GetCouponSourcesFacadeResult.List(emptyList())

            val response = CouponSourcesResponse.from(result)

            assertThat(response.coupons).isEmpty()
        }
    }
}
