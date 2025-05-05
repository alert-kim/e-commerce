package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.coupon.exception.AlreadyUsedCouponException
import kr.hhplus.be.server.domain.coupon.exception.NotOwnedCouponException
import kr.hhplus.be.server.domain.coupon.exception.RequiredCouponIdException
import kr.hhplus.be.server.testutil.mock.CouponMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.Instant

class CouponTest {

    @Nested
    @DisplayName("Id 조회")
    inner class Id {
        @Test
        @DisplayName("Id가 null이 아닌 경우 ID를 반환한다")
        fun returnsId() {
            val id = CouponMock.id()
            val coupon = CouponMock.coupon(id = id)

            val result = coupon.id()

            assertThat(result).isEqualTo(id)
        }

        @Test
        @DisplayName("Id가 null이면 RequiredCouponIdException 예외가 발생한다")
        fun throwsException() {
            val coupon = CouponMock.coupon(id = null)

            assertThrows<RequiredCouponIdException> {
                coupon.id()
            }
        }
    }

    @Nested
    @DisplayName("쿠폰 생성")
    inner class New {
        @Test
        @DisplayName("새로운 쿠폰을 생성한다")
        fun new() {
            val userId = UserMock.id()
            val couponSourceId = CouponSourceId(1L)
            val name = "쿠폰 이름"
            val discountAmount = BigDecimal.valueOf(1000)
            val createdAt = Instant.now()

            val coupon = Coupon.new(
                userId = userId,
                couponSourceId = couponSourceId,
                name = name,
                discountAmount = discountAmount,
                createdAt = createdAt,
            )

            assertAll(
                { assertThat(coupon.userId).isEqualTo(userId) },
                { assertThat(coupon.couponSourceId).isEqualTo(couponSourceId) },
                { assertThat(coupon.name).isEqualTo(name) },
                { assertThat(coupon.discountAmount).isEqualByComparingTo(discountAmount) },
                { assertThat(coupon.createdAt).isEqualTo(createdAt) },
            )
        }
    }

    @Nested
    @DisplayName("쿠폰 사용")
    inner class UseCoupon {
        @Test
        @DisplayName("쿠폰을 사용하고 사용 정보를 반환한다")
        fun use() {
            val coupon = CouponMock.coupon(usedAt = null)
            val beforeUpdatedAt = coupon.updatedAt

            val usedCoupon = coupon.use(coupon.userId)

            assertThat(coupon.usedAt).isNotNull
            assertThat(coupon.updatedAt).isAfter(beforeUpdatedAt)
            assertThat(usedCoupon.id).isEqualTo(coupon.id())
            assertThat(usedCoupon.userId).isEqualTo(coupon.userId)
            assertThat(usedCoupon.discountAmount).isEqualByComparingTo(coupon.discountAmount)
            assertThat(usedCoupon.usedAt).isEqualTo(coupon.usedAt)
        }

        @Test
        @DisplayName("다른 사용자의 쿠폰을 사용하면 NotOwnedCouponException 예외가 발생한다")
        fun notOwnedCoupon() {
            val coupon = CouponMock.coupon(userId = UserMock.id(1), usedAt = null)

            assertThrows<NotOwnedCouponException> {
                coupon.use(UserMock.id(2))
            }
        }

        @Test
        @DisplayName("이미 사용한 쿠폰을 사용하면 AlreadyUsedCouponException 예외가 발생한다")
        fun alreadyUsed() {
            val coupon = CouponMock.coupon(usedAt = Instant.now())

            assertThrows<AlreadyUsedCouponException> {
                coupon.use(coupon.userId)
            }
        }
    }

    @Nested
    @DisplayName("쿠폰 사용 취소")
    inner class CancelCouponUse {
        @Test
        @DisplayName("쿠폰 사용을 취소한다")
        fun cancel() {
            val now = Instant.now()
            val coupon = CouponMock.coupon(usedAt = now)
            val beforeUpdatedAt = coupon.updatedAt

            coupon.cancelUse()

            assertThat(coupon.usedAt).isNull()
            assertThat(coupon.updatedAt).isAfter(beforeUpdatedAt)
        }

        @Test
        @DisplayName("이미 사용 취소된 쿠폰은 변경되지 않는다")
        fun alreadyCanceled() {
            val coupon = CouponMock.coupon(usedAt = null)
            val beforeUpdatedAt = coupon.updatedAt

            coupon.cancelUse()

            assertThat(coupon.usedAt).isNull()
            assertThat(coupon.updatedAt).isEqualTo(beforeUpdatedAt)
        }
    }

    @Nested
    @DisplayName("할인 금액 계산")
    inner class CalculateDiscount {
        @Test
        @DisplayName("총액이 할인 금액보다 크면 할인 금액을 반환한다")
        fun totalGreaterThanDiscount() {
            val totalAmount = BigDecimal.valueOf(1_000)
            val discountAmount = BigDecimal.valueOf(500)
            val coupon = CouponMock.coupon(discountAmount = discountAmount)

            val result = coupon.calculateDiscountAmount(totalAmount)

            assertThat(result).isEqualTo(discountAmount)
        }

        @Test
        @DisplayName("할인 금액이 총액보다 크면 총액을 반환한다")
        fun discountGreaterThanTotal() {
            val totalAmount = BigDecimal.valueOf(1_000)
            val discountAmount = BigDecimal.valueOf(1_500)
            val coupon = CouponMock.coupon(discountAmount = discountAmount)

            val result = coupon.calculateDiscountAmount(totalAmount)

            assertThat(result).isEqualTo(totalAmount)
        }
    }
}
