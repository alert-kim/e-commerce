package kr.hhplus.be.server.application.product

import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.product.ranking.ProductSaleRankingService
import kr.hhplus.be.server.domain.product.ranking.repository.UpdateProductSaleRankingCommand
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ProductSaleRankingEventListenerTest {
    private val saleRankingService = mockk<ProductSaleRankingService>(relaxed = true)
    private val eventListener = ProductRankingEventListener(saleRankingService)

    @Test
    @DisplayName("주문 완료 이벤트를 수신하면, 상품 판매 랭킹 업데이트")
    fun handleOrderCompletedEvent() {
        val event = OrderMock.completedEvent()
        eventListener.handle(event)

        verify {
            saleRankingService.updateRanking(
                UpdateProductSaleRankingCommand(event)
            )
        }
    }
}
