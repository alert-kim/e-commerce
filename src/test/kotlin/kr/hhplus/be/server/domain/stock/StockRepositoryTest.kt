package kr.hhplus.be.server.domain.stock

import io.kotest.assertions.throwables.shouldThrow
import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.testutil.mock.ProductMock
import kr.hhplus.be.server.testutil.mock.StockMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException

class StockRepositoryTest : RepositoryTest() {

    @Autowired
    private lateinit var stockRepository: StockRepository

    @Nested
    @DisplayName("재고 저장")
    inner class SaveStock {

        @Test
        @DisplayName("재고를 저장하고 ID 할당")
        fun save() {
            val stock = StockMock.stock(id = null)

            val saved = stockRepository.save(stock)

            assertThat(saved.id()).isNotNull
            assertThat(saved.productId).isEqualTo(stock.productId)
            assertThat(saved.quantity).isEqualTo(stock.quantity)
        }

        @Test
        @DisplayName("같은 productId로 저장하면 유니크 제약조건 위반 예외 발생")
        fun saveBySameProductId() {
            val productId = ProductMock.id()
            stockRepository.save(StockMock.stock(id = null, productId = productId))

            val sameProductIdStock = StockMock.stock(id = null, productId = productId)
            shouldThrow<DataIntegrityViolationException> {
                stockRepository.save(sameProductIdStock)
            }
        }
    }

    @Nested
    @DisplayName("상품 ID 목록으로 재고 조회")
    inner class FindAllByProductIds {

        @Test
        @DisplayName("상품 ID 목록으로 재고 목록을 조회")
        fun findAllByProductids() {
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
        @DisplayName("존재하지 않는 상품 ID로 조회하면 빈 목록을 반환")
        fun notFoundStocks() {
            val nonExistingProductId = ProductMock.id()

            val foundStocks = stockRepository.findAllByProductIds(listOf(nonExistingProductId))

            assertThat(foundStocks).isEmpty()
        }
    }

    @Nested
    @DisplayName("상품 ID로 재고 조회")
    inner class FindByProductId {

        @Test
        @DisplayName("상품 ID로 재고를 조회")
        fun shouldFindStockByProductId() {
            val savedStock = stockRepository.save(StockMock.stock(id = null))
            val productId = savedStock.productId

            val foundStock = stockRepository.findByProductId(productId)

            assertThat(foundStock).isNotNull
            assertThat(foundStock?.id()).isEqualTo(savedStock.id())
            assertThat(foundStock?.productId).isEqualTo(productId)
            assertThat(foundStock?.quantity).isEqualTo(savedStock.quantity)
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID로 조회하면 null을 반환")
        fun shouldReturnNullForNonExistingProductId() {
            val nonExistingProductId = ProductMock.id()

            val foundStock = stockRepository.findByProductId(nonExistingProductId)

            assertThat(foundStock).isNull()
        }
    }
}
