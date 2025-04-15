package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.coupon.exception.RequiredCouponSourceIdException
import kr.hhplus.be.server.mock.CouponMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CouponSourceTest {
    @Test
    fun `requireId - id가 null이 아닌 경우 id 반환`() {
        val source = CouponMock.source(id = CouponMock.sourceId())

        val result = source.requireId()

        assertThat(result).isEqualTo(source.id)
    }

    @Test
    fun `requireId - id가 null이면 RequiredCouponSourceIdException 발생`() {
        val source = CouponMock.source(id = null)

        assertThrows<RequiredCouponSourceIdException> {
            source.requireId()
        }
    }
}
