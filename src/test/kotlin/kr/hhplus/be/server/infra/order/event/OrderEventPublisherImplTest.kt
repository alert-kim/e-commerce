package kr.hhplus.be.server.infra.order.event

import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.order.event.*
import kr.hhplus.be.server.testutil.mock.CouponMock
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher

@DisplayName("OrderEventPublisher 구현체 테스트 - ApplicationEventPublisher")
class OrderEventPublisherImplTest {

    private lateinit var orderEventPublisher: OrderEventPublisher
    private val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    @BeforeEach
    fun setUp() {
        orderEventPublisher = OrderEventPublisherImpl(applicationEventPublisher)
    }

    @Nested
    @DisplayName("OrderCreatedEvent")
    inner class OrderCreatedEventTest {
        @Test
        @DisplayName("OrderCreatedEvent를 발행한다")
        fun publish() {
            val order = OrderMock.order(id = OrderMock.id())
            val event = OrderCreatedEvent.from(order)

            orderEventPublisher.publish(event)

            verify { applicationEventPublisher.publishEvent(event) }
        }
    }

    @Nested
    @DisplayName("OrderStockPlacedEvent")
    inner class OrderStockPlacedEventTest {
        @Test
        @DisplayName("OrderStockPlacedEvent를 발행한다")
        fun publish() {
            val orderProduct = OrderMock.product(order = OrderMock.order(id = OrderMock.id()))
            val event = OrderStockPlacedEvent.from(orderProduct)

            orderEventPublisher.publish(event)

            verify { applicationEventPublisher.publishEvent(event) }
        }
    }

    @Nested
    @DisplayName("OrderCouponAppliedEvent")
    inner class OrderCouponAppliedEventTest {
        @Test
        @DisplayName("OrderCouponAppliedEvent를 발행한다")
        fun publish() {
            val order = OrderMock.order(id = OrderMock.id(), couponId = CouponMock.id())
            val event = OrderCouponAppliedEvent.from(order = order)

            orderEventPublisher.publish(event)

            verify { applicationEventPublisher.publishEvent(event) }
        }
    }

    @Nested
    @DisplayName("OrderCompletedEvent")
    inner class OrderCompletedEventTest {
        @Test
        @DisplayName("OrderCompletedEvent를 발행한다")
        fun publish() {
            val event = OrderMock.completedEvent()

            orderEventPublisher.publish(event)

            verify { applicationEventPublisher.publishEvent(event) }
        }
    }

    @Nested
    @DisplayName("OrderFailedEvent")
    inner class OrderFailedEventTest {
        @Test
        @DisplayName("OrderFailedEvent를 발행한다")
        fun publish() {
            val event = OrderMock.failedEvent()

            orderEventPublisher.publish(event)

            verify { applicationEventPublisher.publishEvent(event) }
        }
    }

    @Nested
    @DisplayName("OrderMarkedFailedHandledEvent")
    inner class OrderMarkedFailedHandledEventTest {
        @Test
        @DisplayName("OrderMarkedFailedHandledEvent를 발행한다")
        fun publish() {
            val order = OrderMock.order()
            val event = OrderMarkedFailedHandledEvent(
                orderId = order.id(),
                createdAt = order.updatedAt
            )

            orderEventPublisher.publish(event)

            verify { applicationEventPublisher.publishEvent(event) }
        }
    }
}
