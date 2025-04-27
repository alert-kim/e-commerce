package kr.hhplus.be.server.domain.coupon

import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.domain.coupon.exception.OutOfStockCouponSourceException
import kr.hhplus.be.server.domain.coupon.exception.RequiredCouponSourceIdException
import kr.hhplus.be.server.mock.CouponMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.Instant

class CouponSourceTest {
    @Test
    fun `쿠폰 도메인 생성시 수량이 0보다 같거나 크지 않으면 예외 발생`() {
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

    @Test
    fun `id() - id가 null이 아닌 경우 id 반환`() {
        val id = CouponMock.sourceId()
        val source = CouponMock.source(id = id)

        val result = source.id()

        assertThat(result).isEqualTo(id)
    }

    @Test
    fun `id() - id가 null이면 RequiredCouponSourceIdException 발생`() {
        val source = CouponMock.source(id = null)

        assertThrows<RequiredCouponSourceIdException> {
            source.id()
        }
    }

    @Test
    fun `issue - 쿠폰 발급`() {
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
    fun `issue - 쿠폰 발급 - 발급 후 쿠폰 재고가 0이라면, 상태 변경`() {
        val source = CouponMock.source(quantity = 1, status = CouponSourceStatus.ACTIVE)

        source.issue()

        assertAll(
            { assertThat(source.quantity).isEqualTo(0) },
            { assertThat(source.status).isEqualTo(CouponSourceStatus.OUT_OF_STOCK) },
        )
    }

    @Test
    fun `issue - 쿠폰 발급 - 재고부족, OutOfStockCouponSourceException 발생`() {
        val source = CouponMock.source(quantity = 0, status = CouponSourceStatus.OUT_OF_STOCK)

        assertThrows<OutOfStockCouponSourceException> {
            source.issue()
        }
    }
}
