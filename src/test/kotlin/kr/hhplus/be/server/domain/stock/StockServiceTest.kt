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
import kr.hhplus.be.server.domain.stock.command.AllocateStocksCommand
import kr.hhplus.be.server.domain.stock.exception.NotFoundStockException
import kr.hhplus.be.server.domain.stock.result.AllocatedStock
import kr.hhplus.be.server.mock.ProductMock
import kr.hhplus.be.server.mock.StockMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

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

    @Test
    fun `get - 상품 id재고 조회`() {
        val productId = ProductId(100L)
        val stock = Stock(
            id = 1L,
            productId = productId,
            quantity = 10,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        every { repository.findByProductId(productId) } returns stock

        val result = service.getStock(productId)

        assertThat(result.id).isEqualTo(stock.id())
        verify { repository.findByProductId(productId) }
    }

    @Test
    fun `get - 상품에 대한 재고가 없을 경우 - NotFoundStockException`() {
        val productId = ProductId(100L)
        every { repository.findByProductId(productId) } returns null

        shouldThrow<NotFoundStockException> {
            service.getStock(productId)
        }
    }

    @Test
    fun `getStocks - 여러 상품 id에 대한 재고 조회`() {
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

    @Test
    fun `allocate - 재고를 할당한다`() {
        val productId1 = ProductId(100L)
        val productId2 = ProductId(200L)
        val stock1 = mockk<Stock>()
        val stock2 = mockk<Stock>()
        val allocatedStock1 = AllocatedStock(
            productId = productId1,
            quantity = 5,
        )
        val allocatedStock2 = AllocatedStock(
            productId = productId2,
            quantity = 10,
        )
        every { stock1.productId } returns productId1
        every { stock1.allocate(5) } returns allocatedStock1
        every { stock2.productId } returns productId2
        every { stock2.allocate(10) } returns allocatedStock2
        every { repository.findAllByProductIds(setOf(productId1, productId2)) }
            .returns(listOf(stock1, stock2))
        val command = AllocateStocksCommand(
            needStocks = mapOf(
                productId1 to 5,
                productId2 to 10
            )
        )

        val result = service.allocate(command)

        assertThat(result).hasSize(2)
        assertThat(result).containsExactly(allocatedStock1, allocatedStock2)
        verify { repository.findAllByProductIds(setOf(productId1, productId2)) }
    }

    @Test
    fun `allocate - 상품의 재고가 존재하지 않음 - NotFoundStockException`() {
        val productId = ProductMock.id()
        every { repository.findAllByProductIds(setOf(productId)) } returns emptyList()

        val command = AllocateStocksCommand(
            needStocks = mapOf(
                productId to 5,
            )
        )

        shouldThrow<NotFoundStockException> {
            service.allocate(command)
        }
    }
}
