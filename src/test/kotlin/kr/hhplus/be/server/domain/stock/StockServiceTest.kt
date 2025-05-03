package kr.hhplus.be.server.domain.stock


import io.kotest.assertions.throwables.shouldThrow
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.stock.command.AllocateStockCommand
import kr.hhplus.be.server.domain.stock.exception.NotFoundStockException
import kr.hhplus.be.server.domain.stock.result.AllocatedStock
import kr.hhplus.be.server.testutil.mock.ProductMock
import kr.hhplus.be.server.testutil.mock.StockMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class StockServiceTest {

    @InjectMockKs
    private lateinit var service: StockService

    @MockK(relaxed = true)
    private lateinit var repository: StockRepository

    @BeforeEach
    fun setUp() {
        clearMocks(repository)
    }

    @Nested
    @DisplayName("재고 조회")
    inner class GetStocks {

        @Test
        @DisplayName("여러 상품 id에 대한 재고 조회")
        fun getStocks() {
            val productId1 = ProductId(100L)
            val productId2 = ProductId(200L)
            val stock1 = StockMock.stock()
            val stock2 = StockMock.stock()
            every { repository.findAllByProductIds(any()) } returns listOf(stock1, stock2)

            val result = service.getStocks(listOf(productId1, productId2))

            assertThat(result).hasSize(2)
            assertThat(result[0].id).isEqualTo(stock1.id())
            assertThat(result[1].id).isEqualTo(stock2.id())
            verify { repository.findAllByProductIds(listOf(productId1, productId2)) }
        }
    }

    @Nested
    @DisplayName("재고 할당")
    inner class Allocate {

        @Test
        @DisplayName("상품의 재고를 할당")
        fun allocate() {
            val productId = ProductMock.id()
            val quantity = 5
            val stock = mockk<Stock>(relaxed = true)
            val allocatedStock = AllocatedStock(
                productId = productId,
                quantity = quantity
            )
            every { repository.findByProductId(productId) } returns stock
            every { stock.allocate(quantity) } returns allocatedStock

            val result = service.allocate(AllocateStockCommand(productId = productId, quantity = quantity))

            assertThat(result).isEqualTo(allocatedStock)
            verify {
                repository.findByProductId(productId)
                stock.allocate(quantity)
            }
        }

        @Test
        @DisplayName("상품의 재고가 존재하지 않음 - NotFoundStockException")
        fun notFoundStock() {
            val productId = ProductMock.id()
            every { repository.findByProductId(productId) } returns null

            shouldThrow<NotFoundStockException> {
                service.allocate(AllocateStockCommand(productId = productId, quantity = 10))
            }

            verify { repository.findByProductId(productId) }
        }
    }
}
