package kr.hhplus.be.server.interfaces.product.event.order

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.application.product.ProductRankingFacade
import kr.hhplus.be.server.application.product.ProductSaleStatFacade
import kr.hhplus.be.server.application.product.command.CreateProductSaleStatsFacadeCommand
import kr.hhplus.be.server.application.product.command.UpdateProductRankingFacadeCommand
import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.domain.order.event.OrderCompletedEvent
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProductOrderEventListenerTest {

    private lateinit var orderEventListener: ProductOrderEventListener
    private val productRankingFacade = mockk<ProductRankingFacade>(relaxed = true)
    private val productSaleStatFacade = mockk<ProductSaleStatFacade>(relaxed = true)

    @BeforeEach
    fun setUp() {
        orderEventListener = ProductOrderEventListener(productRankingFacade, productSaleStatFacade)
    }

    @Nested
    @DisplayName("OrderCompletedEvent 처리")
    inner class HandleOrderCompletedEvent {

        @Test
        @DisplayName("OrderCompletedEvent를 수신하면 주문 완료 데이터를 전송하고 상품 관련 처리를 한다")
        fun processCompletedOrder() {
            val orderId = OrderMock.id()
            val order = OrderMock.view(id = orderId, status = OrderStatus.COMPLETED)
            val event = OrderCompletedEvent(
                orderId = orderId,
                order = order,
                createdAt = order.updatedAt
            )

            orderEventListener.handle(event)

            verify(exactly = 1) {
                productRankingFacade.updateRanking(UpdateProductRankingFacadeCommand(order))
                productSaleStatFacade.createStats(CreateProductSaleStatsFacadeCommand(order))
            }
        }

        @Test
        @DisplayName("상품 랭킹 업데이트 중 예외가 발생해도 다른 처리는 계속 진행된다")
        fun rankingUpdateFail() {
            val orderId = OrderMock.id()
            val order = OrderMock.view(id = orderId, status = OrderStatus.COMPLETED)
            val event = OrderCompletedEvent(
                orderId = orderId,
                order = order,
                createdAt = order.updatedAt
            )
            val exception = RuntimeException("랭킹 업데이트 실패")
            every { productRankingFacade.updateRanking(any()) } throws exception

            orderEventListener.handle(event)

            verify(exactly = 1) {
                productRankingFacade.updateRanking(UpdateProductRankingFacadeCommand(order))
                productSaleStatFacade.createStats(CreateProductSaleStatsFacadeCommand(order))
            }
        }

        @Test
        @DisplayName("상품 판매 통계 집계 중 예외가 발생해도 다른 처리는 계속 진행된다")
        fun createStatFail() {
            val orderId = OrderMock.id()
            val order = OrderMock.view(id = orderId, status = OrderStatus.COMPLETED)
            val event = OrderCompletedEvent(
                orderId = orderId,
                order = order,
                createdAt = order.updatedAt
            )
            val exception = RuntimeException("통계 생성 실패")
            every { productSaleStatFacade.createStats(any()) } throws exception

            orderEventListener.handle(event)

            verify(exactly = 1) {
                productRankingFacade.updateRanking(UpdateProductRankingFacadeCommand(order))
                productSaleStatFacade.createStats(CreateProductSaleStatsFacadeCommand(order))
            }
        }
    }
}
