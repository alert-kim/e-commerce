package kr.hhplus.be.server.application.order

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kr.hhplus.be.server.application.order.command.PlaceOrderProductProcessorCommand
import kr.hhplus.be.server.application.order.command.RestoreStockOrderProductProcessorCommand
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.command.PlaceStockCommand
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.product.ProductView
import kr.hhplus.be.server.domain.stock.StockService
import kr.hhplus.be.server.domain.stock.command.AllocateStockCommand
import kr.hhplus.be.server.domain.stock.command.RestoreStockCommand
import kr.hhplus.be.server.testutil.mock.OrderMock
import kr.hhplus.be.server.testutil.mock.ProductMock
import kr.hhplus.be.server.testutil.mock.StockMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class OrderProductProcessorTest {

    private lateinit var processor: OrderProductProcessor

    private val orderService = mockk<OrderService>(relaxed = true)
    private val productService = mockk<ProductService>(relaxed = true)
    private val stockService = mockk<StockService>(relaxed = true)


    @BeforeEach
    fun setUp() {
        clearAllMocks()
        processor = OrderProductProcessor(
            orderService = orderService,
            productService = productService,
            stockService = stockService
        )
    }

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
            processor.placeOrderProduct(command)

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

    @Nested
    @DisplayName("재고 복구 처리")
    inner class RestoreOrderProductStock {
        @Test
        @DisplayName("상품의 재고를 복구한다")
        fun restoreOrderProductStock() {
            val productId = ProductMock.id()
            val quantity = 5
            val command = RestoreStockOrderProductProcessorCommand(
                productId = productId,
                quantity = quantity
            )

            processor.restoreOrderProductStock(command)

            verify {
                stockService.restore(
                    RestoreStockCommand(
                        productId = productId,
                        quantity = quantity
                    )
                )
            }
        }
    }
}
