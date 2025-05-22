package kr.hhplus.be.server.interfaces.coupon.api.response

import kr.hhplus.be.server.application.coupon.result.IssueCouponFacadeResult
import kr.hhplus.be.server.testutil.mock.CouponMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import io.mockk.clearAllMocks
import kr.hhplus.be.server.interfaces.coupon.api.response.CouponResponse

class CouponResponseTest {

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Nested
    @DisplayName("응답 생성")
    inner class From {

        @Test
        @DisplayName("IssueCouponFacadeResult로 생성")
        fun fromIssueCouponFacadeResult() {
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
        @DisplayName("CouponView로 생성")
        fun fromCouponView() {
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
}
