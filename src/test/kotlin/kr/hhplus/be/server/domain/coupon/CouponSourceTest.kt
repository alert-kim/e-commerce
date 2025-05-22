package kr.hhplus.be.server.domain.coupon

import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.domain.coupon.exception.OutOfStockCouponSourceException
import kr.hhplus.be.server.domain.coupon.exception.RequiredCouponSourceIdException
import kr.hhplus.be.server.testutil.mock.CouponMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.Instant

class CouponSourceTest {
    
    @Nested
    @DisplayName("생성")
    inner class Create {
        @Test
        @DisplayName("수량이 0보다 작으면 예외 발생")
        fun invalidQuantity() {
            assertThrows<IllegalArgumentException> {
                CouponSource(
                    id = null,
                    name = "쿠폰",
                    discountAmount = BigDecimal.valueOf(1000),
                    status = CouponSourceStatus.OUT_OF_STOCK,
                    quantity = -1,
                    initialQuantity = 0,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                )
            }
        }
    }

    @Nested
    @DisplayName("ID 조회")
    inner class Id {
        @Test
        @DisplayName("ID가 존재하면 반환")
        fun exists() {
            val id = CouponMock.sourceId()
            val source = CouponMock.source(id = id)

            val result = source.id()

            assertThat(result).isEqualTo(id)
        }

        @Test
        @DisplayName("ID가 없으면 예외 발생")
        fun notExists() {
            val source = CouponMock.source(id = null)

            assertThrows<RequiredCouponSourceIdException> {
                source.id()
            }
        }
    }

    @Nested
    @DisplayName("쿠폰 발급")
    inner class Issue {
        @Test
        @DisplayName("쿠폰 발급 성공")
        fun success() {
            val originalQuantity = Arb.int(2..5).next()
            val source = CouponMock.source(quantity = originalQuantity, status = CouponSourceStatus.ACTIVE)

            val result = source.issue()

            assertAll(
                { assertThat(result.couponSourceId).isEqualTo(source.id()) },
                { assertThat(result.name).isEqualTo(source.name) },
                { assertThat(result.discountAmount).isEqualTo(source.discountAmount) },
                { assertThat(source.quantity).isEqualTo(originalQuantity - 1) },
            )
        }

        @Test
        @DisplayName("발급 후 재고가 0이면 상태 변경")
        fun outOfStock() {
            val source = CouponMock.source(quantity = 1, status = CouponSourceStatus.ACTIVE)

            source.issue()

            assertAll(
                { assertThat(source.quantity).isEqualTo(0) },
                { assertThat(source.status).isEqualTo(CouponSourceStatus.OUT_OF_STOCK) },
            )
        }

        @Test
        @DisplayName("재고 부족 시 예외 발생")
        fun insufficientStock() {
            val source = CouponMock.source(quantity = 0, status = CouponSourceStatus.OUT_OF_STOCK)

            assertThrows<OutOfStockCouponSourceException> {
                source.issue()
            }
        }
    }
}
