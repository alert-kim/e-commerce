package kr.hhplus.be.server.application.product

import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.application.product.command.UpdateProductRankingFacadeCommand
import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.domain.order.event.OrderCompletedEvent
import kr.hhplus.be.server.domain.product.ranking.ProductSaleRankingService
import kr.hhplus.be.server.domain.product.ranking.repository.UpdateProductSaleRankingCommand
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProductRankingFacadeTest {

    private lateinit var productRankingFacade: ProductRankingFacade
    private val productSaleRankingService = mockk<ProductSaleRankingService>(relaxed = true)

    @BeforeEach
    fun setUp() {
        productRankingFacade = ProductRankingFacade(productSaleRankingService)
    }

    @Nested
    @DisplayName("상품 판매 순위 업데이트")
    inner class UpdateProductRankingTest {
        @Test
        @DisplayName("상품 판매 순위를 업데이트한다")
        fun updateProductRanking() {
            val orderId = OrderMock.id()
            val order = OrderMock.view(id = orderId, status = OrderStatus.COMPLETED)
            val event = OrderCompletedEvent(
                orderId = orderId,
                order = order,
                createdAt = order.updatedAt
            )
            val command = UpdateProductRankingFacadeCommand(event)

            productRankingFacade.updateRanking(command)

            verify(exactly = 1) {
                productSaleRankingService.updateRanking(
                    UpdateProductSaleRankingCommand(
                        event = event
                    )
                )
            }
        }
    }
}
