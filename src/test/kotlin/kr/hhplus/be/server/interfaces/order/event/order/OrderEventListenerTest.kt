package kr.hhplus.be.server.interfaces.order.event.order

import io.kotest.assertions.throwables.shouldThrowAny
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.application.order.OrderCancelFacade
import kr.hhplus.be.server.application.order.command.CancelOrderFacadeCommand
import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.domain.order.event.OrderFailedEvent
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class OrderEventListenerTest {

    private lateinit var orderEventListener: OrderEventListener
    private val orderCancelFacade = mockk<OrderCancelFacade>(relaxed = true)

    @BeforeEach
    fun setUp() {
        orderEventListener = OrderEventListener(orderCancelFacade)
    }

    @Nested
    @DisplayName("OrderFailedEvent 처리")
    inner class HandleOrderFailedEvent {

        @Test
        @DisplayName("OrderFailedEvent를 수신하면 주문 취소를 진행한다")
        fun cancelOrder() {
            val orderId = OrderMock.id()
            val order = OrderMock.view(id = orderId, status = OrderStatus.FAILED)
            val event = OrderFailedEvent(
                orderId = orderId,
                order = order,
                createdAt = order.updatedAt
            )

            orderEventListener.handle(event)

            verify(exactly = 1) {
                orderCancelFacade.cancel(CancelOrderFacadeCommand(order))
            }
        }

        @Test
        @DisplayName("OrderCancelFacade 호출 중 예외가 발생해도 처리된다 (에러 로그만 남김)")
        fun exceptionOccurred() {
            val orderId = OrderMock.id()
            val order = OrderMock.view(id = orderId, status = OrderStatus.FAILED)
            val event = OrderFailedEvent(
                orderId = orderId,
                order = order,
                createdAt = order.updatedAt
            )
            val exception = RuntimeException("취소 처리 실패")
            every { orderCancelFacade.cancel(any()) } throws exception

            assertDoesNotThrow {
                orderEventListener.handle(event)
            }

            verify(exactly = 1) {
                orderCancelFacade.cancel(CancelOrderFacadeCommand(order))
            }
        }
    }
}
