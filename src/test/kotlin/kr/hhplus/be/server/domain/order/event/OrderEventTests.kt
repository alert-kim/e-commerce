package kr.hhplus.be.server.domain.order.event

import io.kotest.assertions.throwables.shouldThrow
import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.domain.order.exception.InvalidOrderCouponException
import kr.hhplus.be.server.testutil.mock.CouponMock
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OrderEventTest {

    @Nested
    @DisplayName("OrderCreatedEvent")
    inner class OrderCreatedEventTest {
        @Test
        @DisplayName("주문에서 OrderCreatedEvent를 생성한다")
        fun create() {
            val orderId = OrderMock.id()
            val order = OrderMock.order(
                id = orderId,
            )

            val event = OrderCreatedEvent.from(order)

            assertThat(event.orderId).isEqualTo(orderId)
            assertThat(event.userId).isEqualTo(order.userId)
            assertThat(event.createdAt).isEqualTo(order.createdAt)
        }
    }

    @Nested
    @DisplayName("OrderStockPlacedEvent")
    inner class OrderStockPlacedEventTest {
        @Test
        @DisplayName("주문 상품에서 OrderStockPlacedEvent를 생성한다")
        fun create() {
            val orderId = OrderMock.id()
            val order = OrderMock.order(id = orderId)
            val orderProduct = OrderMock.product(
                order = order,
            )

            val event = OrderStockPlacedEvent.from(orderProduct)

            assertThat(event.orderId).isEqualTo(orderId)
            assertThat(event.productId).isEqualTo(orderProduct.productId)
            assertThat(event.quantity).isEqualTo(orderProduct.quantity)
            assertThat(event.unitPrice).isEqualByComparingTo(orderProduct.unitPrice)
        }
    }

    @Nested
    @DisplayName("OrderCouponAppliedEvent")
    inner class OrderCouponAppliedEventTest {
        @Test
        @DisplayName("쿠폰이 적용된 주문에서 OrderCouponAppliedEvent를 생성한다")
        fun create() {
            val orderId = OrderMock.id()
            val couponId = CouponMock.id()
            val order = OrderMock.order(
                id = orderId,
                couponId = couponId,
            )

            val event = OrderCouponAppliedEvent.from(order)

            assertThat(event.orderId).isEqualTo(orderId)
            assertThat(event.couponId).isEqualTo(couponId)
            assertThat(event.discCountAmount).isEqualByComparingTo(order.discountAmount)
            assertThat(event.totalAmount).isEqualByComparingTo(order.totalAmount)
        }

        @Test
        @DisplayName("쿠폰이 적용되지 않은 주문에서 OrderCouponAppliedEvent 생성 시 예외가 발생한다")
        fun couponNotApplied() {
            val order = OrderMock.order(couponId = null)

            shouldThrow<InvalidOrderCouponException> {
                OrderCouponAppliedEvent.from(order)
            }
        }
    }

    @Nested
    @DisplayName("OrderCompletedEvent")
    inner class OrderCompletedEventTest {
        @Test
        @DisplayName("주문에서 OrderCompletedEvent를 생성한다")
        fun create() {
            val orderId = OrderMock.id()
            val order = OrderMock.order(
                id = orderId,
                status = OrderStatus.COMPLETED,
            )

            val event = OrderCompletedEvent.from(order)

            assertThat(event.orderId).isEqualTo(orderId)
            assertThat(event.order.id).isEqualTo(orderId)
            assertThat(event.order.status).isEqualTo(OrderStatus.COMPLETED)
        }
    }

    @Nested
    @DisplayName("OrderFailedEvent")
    inner class OrderFailedEventTest {
        @Test
        @DisplayName("주문에서 OrderFailedEvent를 생성한다")
        fun create() {
            val orderId = OrderMock.id()
            val order = OrderMock.order(
                id = orderId,
                status = OrderStatus.FAILED,
            )

            val event = OrderFailedEvent.from(order)

            assertThat(event.orderId).isEqualTo(orderId)
            assertThat(event.order.id).isEqualTo(orderId)
            assertThat(event.order.status).isEqualTo(OrderStatus.FAILED)
        }
    }

    @Nested
    @DisplayName("OrderMarkedFailedHandledEvent")
    inner class OrderMarkedFailedHandledEventTest {
        @Test
        @DisplayName("주문에서 OrderMarkedFailedHandledEvent를 생성한다")
        fun create() {
            val orderId = OrderMock.id()
            val order = OrderMock.order(
                id = orderId,
                status = OrderStatus.FAIL_HANDLED,
            )

            val event = OrderMarkedFailedHandledEvent.from(order)

            assertThat(event.orderId).isEqualTo(orderId)
        }
    }
}
