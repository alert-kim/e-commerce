package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.coupon.exception.RequiredCouponIdException
import kr.hhplus.be.server.mock.CouponMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CouponTest {
    @Test
    fun `requireId - id가 null이 아닌 경우 id 반환`() {
        val coupon = CouponMock.coupon(id = CouponMock.id())

        val result = coupon.requireId()

        assertThat(result).isEqualTo(coupon.id)
    }

    @Test
    fun `requireId - id가 null이면 RequiredCouponIdException 발생`() {
        val coupon = CouponMock.coupon(id = null)

        assertThrows<RequiredCouponIdException> {
            coupon.requireId()
        }
    }
}
