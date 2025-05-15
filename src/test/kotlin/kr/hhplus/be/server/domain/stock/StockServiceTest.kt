package kr.hhplus.be.server.domain.stock

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.stock.command.AllocateStockCommand
import kr.hhplus.be.server.domain.stock.command.RestoreStockCommand
import kr.hhplus.be.server.domain.stock.exception.NotFoundStockException
import kr.hhplus.be.server.domain.stock.result.AllocatedStock
import kr.hhplus.be.server.testutil.assertion.StockViewListAssert.Companion.assertStockViews
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

    @MockK
    private lateinit var cacheReader: StockCacheReader

    @MockK(relaxed = true)
    private lateinit var repository: StockRepository

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Nested
    @DisplayName("재고 조회")
    inner class GetStocks {

        @Test
        @DisplayName("여러 상품 id에 대한 재고 조회")
        fun getStocks() {
            val stocks = List(3) { StockMock.view() }
            stocks.forEach { stock ->
                every { cacheReader.getOrNullByProductId(stock.productId) } returns stock
            }
            val productIds = stocks.map { it.productId }

            val result = service.getStocks(productIds)

            assertStockViews(result).isEqualTo(stocks)
            verify {
                productIds.forEach { productId ->
                    cacheReader.getOrNullByProductId(productId)
                }
            }
        }
        
        @Test
        @DisplayName("일부 상품이 존재하지 않는 경우 존재하는 상품만 반환")
        fun getSomeStocks() {
            val notExistsProductId = ProductMock.id()
            val existsStocks = List(3) { StockMock.view() }
            val allProductIds = existsStocks.map { it.productId } + notExistsProductId
            existsStocks.forEach { stock ->
                every { cacheReader.getOrNullByProductId(stock.productId) } returns stock
            }
            every { cacheReader.getOrNullByProductId(notExistsProductId) } returns null

            val result = service.getStocks(allProductIds)

            assertStockViews(result).isEqualTo(existsStocks)
            verify {
                allProductIds.forEach { productId ->
                    cacheReader.getOrNullByProductId(productId)
                }
            }
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

    @Nested
    @DisplayName("재고 복구")
    inner class Restore {
        @Test
        @DisplayName("상품의 재고를 복구")
        fun restore() {
            val productId = ProductMock.id()
            val quantity = 5
            val stock = mockk<Stock>(relaxed = true)
            every { repository.findByProductId(productId) } returns stock

            service.restore(RestoreStockCommand(productId = productId, quantity = quantity))

            verify {
                repository.findByProductId(productId)
                stock.restore(quantity)
            }
        }

        @Test
        @DisplayName("상품의 재고가 존재하지 않음 - NotFoundStockException")
        fun notFoundStock() {
            val productId = ProductMock.id()
            every { repository.findByProductId(productId) } returns null

            shouldThrow<NotFoundStockException> {
                service.restore(RestoreStockCommand(productId = productId, quantity = 10))
            }

            verify { repository.findByProductId(productId) }
        }
    }
}
