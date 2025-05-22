package kr.hhplus.be.server.application.order

import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.application.order.command.SendCompletedOrderFacadeCommand
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.domain.order.command.SendOrderCompletedCommand
import kr.hhplus.be.server.domain.order.exception.InvalidOrderStatusException
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrderSendFacadeTest {

    private lateinit var orderSendFacade: OrderSendFacade
    private val orderService = mockk<OrderService>(relaxed = true)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        orderSendFacade = OrderSendFacade(orderService)
    }

    @Test
    @DisplayName("완료된 주문을 전송한다")
    fun sendCompletedOrder() {
        val order = OrderMock.view(status = OrderStatus.COMPLETED)

        orderSendFacade.sendCompleted(SendCompletedOrderFacadeCommand(order))

        verify(exactly = 1) {
            orderService.sendOrderCompleted(
                SendOrderCompletedCommand(order)
            )
        }
    }

    @Test
    @DisplayName("완료되지 않은 주문은 전송할 수 없다")
    fun notCompletedOrder() {
        val order = OrderMock.view(status = OrderStatus.STOCK_ALLOCATED)

        assertThrows<InvalidOrderStatusException> {
            orderSendFacade.sendCompleted(SendCompletedOrderFacadeCommand(order))
        }

        verify(exactly = 0) {
            orderService.sendOrderCompleted(any())
        }
    }
}
