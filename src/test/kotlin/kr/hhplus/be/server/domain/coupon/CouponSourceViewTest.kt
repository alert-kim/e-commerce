package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.coupon.exception.RequiredCouponSourceIdException
import kr.hhplus.be.server.testutil.mock.CouponMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CouponSourceViewTest {
    @Nested
    @DisplayName("from")
    inner class From {
        @Test
        @DisplayName("쿠폰 소스 정보를 올바르게 변환한다")
        fun success() {
            val coupon = CouponMock.source()

            val result = CouponSourceView.from(coupon)

            assertThat(result.id).isEqualTo(coupon.id())
        }

        @Test
        @DisplayName("아이디가 null이면 RequiredCouponSourceIdException가 발생한다")
        fun nullId() {
            val coupon = CouponMock.source(id = null)

            assertThrows<RequiredCouponSourceIdException> {
                CouponSourceView.from(coupon)
            }
        }
    }
}
