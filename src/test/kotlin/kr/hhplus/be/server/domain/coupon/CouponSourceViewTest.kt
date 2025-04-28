package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.coupon.exception.RequiredCouponSourceIdException
import kr.hhplus.be.server.mock.CouponMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CouponSourceViewTest {
    @Test
    fun `쿠폰 소스 정보를 올바르게 변환한다`() {
        val coupon = CouponMock.source()

        val result = CouponSourceView.from(coupon)

        assertThat(result.id).isEqualTo(coupon.id())
    }

    @Test
    fun `해당 상품의 아이디가 null이면 RequiredCouponSourceIdException가 발생한다`() {
        val coupon = CouponMock.source(id = null)

        assertThrows<RequiredCouponSourceIdException> {
            CouponSourceView.from(coupon)
        }
    }
}
