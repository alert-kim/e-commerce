package kr.hhplus.be.server.application.order

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kr.hhplus.be.server.application.order.command.PlaceOrderProductProcessorCommand
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.command.PlaceStockCommand
import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.product.ProductView
import kr.hhplus.be.server.domain.stock.StockService
import kr.hhplus.be.server.domain.stock.command.AllocateStockCommand
import kr.hhplus.be.server.domain.stock.result.AllocatedStock
import kr.hhplus.be.server.testutil.mock.OrderMock
import kr.hhplus.be.server.testutil.mock.ProductMock
import kr.hhplus.be.server.testutil.mock.StockMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class OrderProductProcessorTest {

    @InjectMockKs
    private lateinit var orderProductProcessor: OrderProductProcessor

    @MockK(relaxed = true)
    private lateinit var orderService: OrderService

    @MockK(relaxed = true)
    private lateinit var productService: ProductService

    @MockK(relaxed = true)
    private lateinit var stockService: StockService

    @Nested
    @DisplayName("상품 주문 처리")
    inner class Place {
        @Test
        @DisplayName("상품을 확인하고, 재고를 할당하고, 주문에 상품을 배치한다")
        fun placeOrderProduct() {
            val orderId = OrderMock.id()
            val productId = ProductMock.id()
            val product = mockk<ProductView>(relaxed = true)
            val purchasableProduct = ProductMock.purchasableProduct(id = productId)
            val allocatedStock = StockMock.allocated(productId = productId)
            every { product.id } returns productId
            every { productService.get(productId.value) } returns product
            every { product.validatePurchasable(any()) } returns purchasableProduct
            every { stockService.allocate(any<AllocateStockCommand>()) } returns allocatedStock

            val unitPrice = BigDecimal.valueOf(10000)
            val quantity = 2
            val command = PlaceOrderProductProcessorCommand(
                orderId = orderId,
                productId = productId.value,
                unitPrice = unitPrice,
                quantity = quantity
            )
            orderProductProcessor.placeOrderProduct(command)

            verifyOrder {
                productService.get(productId.value)
                product.validatePurchasable(withArg<BigDecimal> {
                    assertThat(it).isEqualByComparingTo(unitPrice)
                })
                stockService.allocate(
                    AllocateStockCommand(
                        productId = purchasableProduct.id,
                        quantity = quantity
                    )
                )
                orderService.placeStock(
                    PlaceStockCommand(
                        orderId = orderId,
                        product = purchasableProduct,
                        stock = allocatedStock
                    )
                )
            }
        }
    }
}
