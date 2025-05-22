package kr.hhplus.be.server.interfaces.coupon.api.response

import kr.hhplus.be.server.application.coupon.result.GetCouponFacadeResult
import kr.hhplus.be.server.testutil.mock.CouponMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import io.mockk.clearAllMocks
import kr.hhplus.be.server.interfaces.coupon.api.response.CouponsResponse

class CouponsResponseTest {

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Nested
    @DisplayName("응답 생성")
    inner class From {

        @Test
        @DisplayName("쿠폰 목록 응답 생성")
        fun fromList() {
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
        @DisplayName("빈 리스트 응답 생성")
        fun fromEmptyList() {
            val result = GetCouponFacadeResult.List(emptyList())

            val response = CouponsResponse.from(result)

            assertThat(response.coupons).isEmpty()
        }
    }
}
