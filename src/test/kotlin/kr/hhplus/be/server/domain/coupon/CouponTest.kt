package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.coupon.exception.AlreadyUsedCouponException
import kr.hhplus.be.server.domain.coupon.exception.NotOwnedCouponException
import kr.hhplus.be.server.domain.coupon.exception.RequiredCouponIdException
import kr.hhplus.be.server.domain.user.UserId
import kr.hhplus.be.server.mock.CouponMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.Instant

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

    @Test
    fun `use - 쿠폰 사용`() {
        val coupon = CouponMock.coupon(usedAt = null)

        coupon.use(coupon.userId)

        assertThat(coupon.usedAt).isNotNull
    }

    @Test
    fun `use - 다른 사람 쿠폰 사용 - NotOwnedCouponException 발생`() {
        val coupon = CouponMock.coupon(userId = UserId(1L), usedAt = null)

        assertThrows<NotOwnedCouponException> {
            coupon.use(UserId(2L))
        }
    }

    @Test
    fun `use - 이미 사용한 쿠폰 - AlreadyUsedCouponException 발생`() {
        val coupon = CouponMock.coupon(usedAt = Instant.now())

        assertThrows<AlreadyUsedCouponException> {
            coupon.use(coupon.userId)
        }
    }

    @Test
    fun `calculateDiscountAmount - 할인 금액 계산 (total이 discount보다 큼)`() {
        val totalAmount = BigDecimal.valueOf(1_000)
        val discountAmount = BigDecimal.valueOf(500)
        val coupon = CouponMock.coupon(discountAmount = discountAmount)

        val result = coupon.calculateDiscountAmount(totalAmount)

        assertThat(result).isEqualTo(discountAmount)
    }

    @Test
    fun `calculateDiscountAmount - 할인 금액 계산 (discount가 total보다 큼)`() {
        val totalAmount = BigDecimal.valueOf(1_000)
        val discountAmount = BigDecimal.valueOf(1_500)
        val coupon = CouponMock.coupon(discountAmount = discountAmount)

        val result = coupon.calculateDiscountAmount(totalAmount)

        assertThat(result).isEqualTo(totalAmount)
    }
}
