package kr.hhplus.be.server.domain.stock

import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.testutil.mock.ProductMock
import kr.hhplus.be.server.testutil.mock.StockMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class StockRepositoryTest : RepositoryTest() {

    @Autowired
    private lateinit var stockRepository: StockRepository

    @Test
    fun `save - 재고를 저장하고 ID가 할당됨`() {
        val stock = StockMock.stock(id = null)

        val saved = stockRepository.save(stock)

        assertThat(saved.id()).isNotNull
        assertThat(saved.productId).isEqualTo(stock.productId)
        assertThat(saved.quantity).isEqualTo(stock.quantity)
    }

    @Test
    fun `findAllByProductIds - 상품 ID 목록으로 재고 목록을 조회한다`() {
        val stocks = List(2) {
            stockRepository.save(
                StockMock.stock(id = null)
            )
        }
        val productIds = stocks.map { it.productId }
        stockRepository.save(
            StockMock.stock(id = null, productId = ProductMock.id())
        )

        val foundStocks = stockRepository.findAllByProductIds(productIds)

        assertThat(foundStocks).hasSize(productIds.size)
        foundStocks.forEach { stock ->
            val target = stocks.find { it.id() == stock.id() }
            assertThat(target).isNotNull
            assertThat(stock.productId).isEqualTo(target?.productId)
            assertThat(stock.quantity).isEqualTo(target?.quantity)
        }
    }

    @Test
    fun `findAllByProductIds - 존재하지 않는 상품 ID로 조회하면 빈 목록을 반환한다`() {
        val nonExistingProductId = ProductMock.id()

        val foundStocks = stockRepository.findAllByProductIds(listOf(nonExistingProductId))

        assertThat(foundStocks).isEmpty()
    }
}
