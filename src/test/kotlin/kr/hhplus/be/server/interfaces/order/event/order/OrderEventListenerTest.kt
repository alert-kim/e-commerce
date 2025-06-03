package kr.hhplus.be.server.interfaces.order.event.order

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.application.order.OrderCancelFacade
import kr.hhplus.be.server.application.order.OrderSendFacade
import kr.hhplus.be.server.application.order.command.CancelOrderFacadeCommand
import kr.hhplus.be.server.application.order.command.SendCompletedOrderFacadeCommand
import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.domain.order.event.OrderCompletedEvent
import kr.hhplus.be.server.domain.order.event.OrderFailedEvent
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.junit.jupiter.api.*

class OrderEventListenerTest {

    private lateinit var orderEventListener: OrderEventListener
    private val orderCancelFacade = mockk<OrderCancelFacade>(relaxed = true)
    private val orderSendFacade = mockk<OrderSendFacade>(relaxed = true)

    @BeforeEach
    fun setUp() {
        orderEventListener = OrderEventListener(orderCancelFacade, orderSendFacade)
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
    
    @Nested
    @DisplayName("OrderCompletedEvent 처리")
    inner class HandleOrderCompletedEvent {
        
        @Test
        @DisplayName("OrderCompletedEvent를 수신하면 주문 완료 데이터를 전송한다")
        fun sendCompletedOrder() {
            val orderId = OrderMock.id()
            val order = OrderMock.view(id = orderId, status = OrderStatus.COMPLETED)
            val event = OrderCompletedEvent(
                orderId = orderId,
                order = order,
                createdAt = order.updatedAt
            )
            
            orderEventListener.handle(event)
            
            verify(exactly = 1) {
                orderSendFacade.sendCompleted(SendCompletedOrderFacadeCommand(order))
            }
        }
        
        @Test
        @DisplayName("OrderSendFacade 호출 중 예외가 발생해도 처리된다 (에러 로그만 남김)")
        fun exceptionOccurred() {
            val orderId = OrderMock.id()
            val order = OrderMock.view(id = orderId, status = OrderStatus.COMPLETED)
            val event = OrderCompletedEvent(
                orderId = orderId,
                order = order,
                createdAt = order.updatedAt
            )
            every { orderSendFacade.sendCompleted(any()) } throws RuntimeException("데이터 전송 실패")
            
            assertDoesNotThrow {
                orderEventListener.handle(event)
            }
            
            verify(exactly = 1) {
                orderSendFacade.sendCompleted(SendCompletedOrderFacadeCommand(order))
            }
        }
    }
}
