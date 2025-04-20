package kr.hhplus.be.server.domain.order

import io.kotest.assertions.throwables.shouldThrow
import kr.hhplus.be.server.domain.coupon.CouponId
import kr.hhplus.be.server.domain.order.exception.AlreadyCouponAppliedException
import kr.hhplus.be.server.domain.order.exception.InvalidOrderStatusException
import kr.hhplus.be.server.domain.order.exception.RequiredOrderIdException
import kr.hhplus.be.server.mock.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.Instant

class OrderTest {
    @Test
    fun `requireId - id가 null이 아닌 경우 id 반환`() {
        val order = OrderMock.order(id = OrderMock.id())

        val result = order.requireId()

        assertThat(result).isEqualTo(order.id)
    }

    @Test
    fun `requireId - id가 null이면 RequiredOrderIdException 발생`() {
        val order = OrderMock.order(id = null)

        assertThrows<RequiredOrderIdException> {
            order.requireId()
        }
    }

    @Test
    fun `new - 해당 유저아이디를 가진, 0원으로 초기화된 주문을 생성한다`() {
        val userId = UserMock.id()

        val order = Order.new(userId)

        assertAll(
            { assertThat(order.userId).isEqualTo(userId) },
            { assertThat(order.status).isEqualTo(OrderStatus.READY) },
            { assertThat(order.originalAmount).isEqualByComparingTo(BigDecimal.ZERO) },
            { assertThat(order.discountAmount).isEqualByComparingTo(BigDecimal.ZERO) },
            { assertThat(order.totalAmount).isEqualByComparingTo(BigDecimal.ZERO) },
            { assertThat(order.products).isEmpty() },
            { assertThat(order.couponId).isNull() },
        )
    }

    @Test
    fun `placeStock - 주문에 상품을 추가한다`() {
        val order = OrderMock.order(
            status = OrderStatus.READY,
            products = emptyList(),
            originalAmount = BigDecimal.ZERO,
            discountAmount = BigDecimal.ZERO,
            totalAmount = BigDecimal.ZERO,
        )
        val productStocks = List(2) {
            ProductMock.stockAllocated()
        }
        val totalAmount = productStocks.sumOf { it.unitPrice.multiply(BigDecimal(it.quantity.toLong())) }

        order.placeStock(productStocks)

        assertAll(
            { assertThat(order.products).hasSize(productStocks.size) },
            { assertThat(order.originalAmount).isEqualByComparingTo(totalAmount) },
            { assertThat(order.totalAmount).isEqualByComparingTo(totalAmount) },
        )
        order.products.forEachIndexed { index, orderProduct ->
            val productStock = productStocks[index]
            assertAll(
                { assertThat(orderProduct.orderId).isEqualTo(order.id) },
                { assertThat(orderProduct.productId).isEqualTo(productStock.productId) },
                { assertThat(orderProduct.quantity).isEqualTo(productStock.quantity) },
                { assertThat(orderProduct.unitPrice).isEqualByComparingTo(productStock.unitPrice) },
                {
                    assertThat(orderProduct.totalPrice).isEqualByComparingTo(
                        productStock.unitPrice.multiply(BigDecimal.valueOf(productStock.quantity.toLong()))
                    )
                },
            )
        }
    }

    @Test
    fun `placeStock - 주문 상태가 READY가 아니면 InvalidOrderStatusException 발생`() {
        val order = OrderMock.order(
            status = OrderStatus.COMPLETED,
        )
        val productStocks = List(2) {
            ProductMock.stockAllocated()
        }

        assertThrows<InvalidOrderStatusException> {
            order.placeStock(productStocks)
        }
    }

    @Test
    fun `applyCoupon - 쿠폰 적용`() {
        val updatedAt = Instant.now()
        val originalAmount = BigDecimal.valueOf(1_500)
        val discountAmount = BigDecimal.valueOf(500)
        val expectTotalAmount = BigDecimal.valueOf(1_000)
        val order = OrderMock.order(
            status = OrderStatus.STOCK_ALLOCATED,
            couponId = null,
            totalAmount = originalAmount,
            originalAmount = originalAmount,
            discountAmount = BigDecimal.ZERO,
            updatedAt = updatedAt,
        )
        val couponId = CouponMock.id()
        val usedCoupon = CouponMock.usedCoupon(
            id = couponId,
            discountAmount = discountAmount,
        )

        order.applyCoupon(usedCoupon)

        assertAll(
            { assertThat(order.couponId).isEqualTo(couponId) },
            { assertThat(order.status).isEqualTo(OrderStatus.STOCK_ALLOCATED) },
            { assertThat(order.originalAmount).isEqualByComparingTo(order.originalAmount) },
            { assertThat(order.discountAmount).isEqualByComparingTo(discountAmount) },
            { assertThat(order.totalAmount).isEqualByComparingTo(expectTotalAmount) },
            { assertThat(order.updatedAt).isAfter(updatedAt) },
        )
    }

    @Test
    fun `applyCoupon - 같은 쿠폰이 적용되어 있으면 업데이트 되지 않는다`() {
        val couponId = CouponId(1L)
        val usedCoupon = CouponMock.usedCoupon(
            id = couponId,
        )
        val updatedAt = Instant.now()
        val order = OrderMock.order(
            status = OrderStatus.STOCK_ALLOCATED,
            couponId = couponId,
            updatedAt = updatedAt,
        )

        order.applyCoupon(usedCoupon)

        assertAll(
            { assertThat(order.couponId).isEqualTo(couponId) },
            { assertThat(order.discountAmount).isEqualByComparingTo(order.discountAmount) },
            { assertThat(order.totalAmount).isEqualByComparingTo(order.totalAmount) },
        )
    }

    @Test
    fun `applyCoupon - 다른 쿠폰이 적용되어 있으면 AlreadyCouponAppliedException 발생`() {
        val couponId = CouponId(2L)
        val usedCoupon = CouponMock.usedCoupon(
            id = couponId,
        )
        val order = OrderMock.order(
            status = OrderStatus.STOCK_ALLOCATED,
            couponId = CouponId(1L),
        )

        shouldThrow<AlreadyCouponAppliedException> {
            order.applyCoupon(usedCoupon)
        }
    }

    @Test
    fun `applyCoupon - 주문 상태가 STOCK_ALLOCATED가 아니면 InvalidOrderStatusException 발생`() {
        val usedCoupon = CouponMock.usedCoupon(
            id = CouponMock.id(),
        )
        val order = OrderMock.order(
            status = OrderStatus.COMPLETED,
        )

        assertThrows<InvalidOrderStatusException> {
            order.applyCoupon(usedCoupon)
        }
    }

    @Test
    fun `pay - 결제 처리`() {
        val order = OrderMock.order(
            status = OrderStatus.STOCK_ALLOCATED,
        )

        order.pay()

        assertAll(
            { assertThat(order.status).isEqualTo(OrderStatus.COMPLETED) },
            { assertThat(order.updatedAt).isAfter(order.createdAt) },
        )
    }

    @Test
    fun `pay - 주문 상태가 STOCK_ALLOCATED가 아니면 InvalidOrderStatusException 발생`() {
        val order = OrderMock.order(
            status = OrderStatus.READY,
        )

        assertThrows<InvalidOrderStatusException> {
            order.pay()
        }
    }
}
