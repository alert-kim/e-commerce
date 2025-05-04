package kr.hhplus.be.server.application.product

import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.product.stat.ProductSaleStatService
import kr.hhplus.be.server.domain.product.stat.command.CreateProductSaleStatsCommand
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ProductStatEventListenerTest {
    private val service = mockk<ProductSaleStatService>(relaxed = true)
    private val eventListener = ProductSaleStatEventListener(service)

    @Test
    @DisplayName("주문 완료 이벤트를 수신하면, 상품 판매 통계 데이터를 생성")
    fun handleOrderCompletedEvent() {
        val event = OrderMock.completedEvent()
        eventListener.handle(event)

        verify {
            service.createStats(
                CreateProductSaleStatsCommand(event)
            )
        }
    }
}
