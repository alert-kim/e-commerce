package kr.hhplus.be.server.interfaces.product.event.order

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.application.product.ProductRankingFacade
import kr.hhplus.be.server.application.product.command.UpdateProductRankingFacadeCommand
import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.domain.order.event.OrderCompletedEvent
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class ProductOrderEventListenerTest {

    private lateinit var orderEventListener: ProductOrderEventListener
    private val productRankingFacade = mockk<ProductRankingFacade>(relaxed = true)

    @BeforeEach
    fun setUp() {
        orderEventListener = ProductOrderEventListener(productRankingFacade)
    }

    @Test
    @DisplayName("OrderCompletedEvent를 수신하면 상품 판매 순위를 업데이트한다")
    fun updateProductRanking() {
        val orderId = OrderMock.id()
        val order = OrderMock.view(id = orderId, status = OrderStatus.COMPLETED)
        val event = OrderCompletedEvent(
            orderId = orderId,
            order = order,
            createdAt = order.updatedAt
        )

        orderEventListener.handle(event)

        verify(exactly = 1) {
            productRankingFacade.updateRanking(UpdateProductRankingFacadeCommand(event))
        }
    }

    @Test
    @DisplayName("ProductRankingFacade 호출 중 예외가 발생해도 처리된다 (에러 로그만 남김)")
    fun exceptionOccurred() {
        val orderId = OrderMock.id()
        val order = OrderMock.view(id = orderId, status = OrderStatus.COMPLETED)
        val event = OrderCompletedEvent(
            orderId = orderId,
            order = order,
            createdAt = order.updatedAt
        )
        val exception = RuntimeException("상품 랭킹 업데이트 실패")
        every { productRankingFacade.updateRanking(any()) } throws exception

        assertDoesNotThrow {
            orderEventListener.handle(event)
        }

        verify(exactly = 1) {
            productRankingFacade.updateRanking(UpdateProductRankingFacadeCommand(event))
        }
    }
}
