package kr.hhplus.be.server.application.product

import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.application.product.command.CreateProductSaleStatsFacadeCommand
import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.domain.product.stat.ProductSaleStatService
import kr.hhplus.be.server.domain.product.stat.command.CreateProductSaleStatsCommand
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ProductSaleStatFacadeTest {

    private lateinit var productSaleStatFacade: ProductSaleStatFacade
    private val productSaleStatService = mockk<ProductSaleStatService>(relaxed = true)

    @BeforeEach
    fun setUp() {
        productSaleStatFacade = ProductSaleStatFacade(productSaleStatService)
    }

    @Test
    @DisplayName("상품 판매 통계를 생성한다")
    fun createProductSaleStat() {
        val orderId = OrderMock.id()
        val order = OrderMock.view(id = orderId, status = OrderStatus.COMPLETED)

        val command = CreateProductSaleStatsFacadeCommand(order)
        productSaleStatFacade.createStats(command)

        verify(exactly = 1) {
            productSaleStatService.createStats(CreateProductSaleStatsCommand(order))
        }
    }
}
