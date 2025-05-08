package kr.hhplus.be.server.domain.stock

import io.kotest.assertions.throwables.shouldThrow
import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.testutil.mock.ProductMock
import kr.hhplus.be.server.testutil.mock.StockMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException

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
    fun `save - 같은 productId로 저장하면 유니크 제약조건 위반 예외 발생`() {
        val productId = ProductMock.id()
        stockRepository.save(StockMock.stock(id = null, productId = productId))

        val sameProductIdStock = StockMock.stock(id = null, productId = productId)
        shouldThrow<DataIntegrityViolationException> {
            stockRepository.save(sameProductIdStock)
        }
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
