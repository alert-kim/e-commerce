package kr.hhplus.be.server.application.order

import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.command.SendOrderCompletedCommand
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OrderDataSenderEventListenerTest {
    private val service: OrderService = mockk<OrderService>(relaxed = true)
    private val eventListener = OrderDataSenderEventListener(service)

    @Nested
    @DisplayName("주문 완료 이벤트 처리")
    inner class HandleOrderCompletedEvent {

        @Test
        @DisplayName("주문 완료 이벤트를 수신하면, 주문 완료 데이터 전송")
        fun sendOrderCompletedData() {
            val event = OrderMock.completedEvent()
            eventListener.handle(event)

            verify {
                service.sendOrderCompleted(
                    SendOrderCompletedCommand(event.order)
                )
            }
        }
    }
}
