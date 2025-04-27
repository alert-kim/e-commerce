package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.domain.product.repository.ProductDailySaleRepository
import kr.hhplus.be.server.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class ProductDailySaleRepositoryTest : RepositoryTest() {
    @Autowired
    lateinit var repository: ProductDailySaleRepository

    @Test
    fun `save - 판매 데이터 저장`() {
        val sale = ProductMock.dailySale()

        val saved = repository.save(sale)

        assertThat(saved.productId).isEqualTo(sale.productId)
        assertThat(saved.date).isEqualTo(sale.date)
        assertThat(saved.quantity).isEqualTo(sale.quantity)
    }

    @Test
    fun `findById - 판매 데이터 조회`() {
        val sale = ProductMock.dailySale()
        val saved = repository.save(sale)

        val found = repository.findById(saved.id)

        assertThat(found).isNotNull
        assertThat(found?.productId).isEqualTo(sale.productId)
        assertThat(found?.date).isEqualTo(sale.date)
        assertThat(found?.quantity).isEqualTo(sale.quantity)
    }

    @Test
    fun `findTopNProductsByQuantity - 판매량 상위 N개 상품 조회 - limit 보다 상품 수가 많음`() {
        val limit = 3
        val startDate = LocalDate.now().minusDays(3)
        val endDate = LocalDate.now()
        val productsInOrderBySale = List(limit + 1) {
            repository.save(ProductMock.dailySale(date = startDate, quantity = 10 - it))
        }

        val result = repository.findTopNProductsByQuantity(startDate, endDate, limit)

        assertThat(result).hasSize(limit)
        result.forEachIndexed { index, productDailySale ->
            assertThat(productDailySale.productId).isEqualTo(productsInOrderBySale[index].productId)
            assertThat(productDailySale.date).isEqualTo(productsInOrderBySale[index].date)
            assertThat(productDailySale.quantity).isEqualTo(productsInOrderBySale[index].quantity)
        }
    }

    @Test
    fun `findTopNProductsByQuantity - 판매량 상위 N개 상품 조회 - limit 보다 상품 수가 적음`() {
        val limit = 3
        val startDate = LocalDate.now().minusDays(3)
        val endDate = LocalDate.now()
        val productsInOrderBySale = List(limit - 1) {
            repository.save(ProductMock.dailySale(date = startDate, quantity = 10 - it))
        }

        val result = repository.findTopNProductsByQuantity(startDate, endDate, limit)

        assertThat(result).hasSize(productsInOrderBySale.size)
    }

    @Test
    fun `findTopNProductsByQuantity - 판매량 상위 N개 상품 조회 - 기준일 이외 날짜는 조회하지 않음`() {
        val limit = 4
        val startDate = LocalDate.now().minusDays(3)
        val endDate = LocalDate.now()
        repository.save(ProductMock.dailySale(date = startDate.minusDays(1), quantity = 10))
        repository.save(ProductMock.dailySale(date = endDate.plusDays(1), quantity = 10))

        val result = repository.findTopNProductsByQuantity(startDate, endDate, limit)

        assertThat(result).isEmpty()
    }

    @Test
    fun `findTopNProductsByQuantity - 판매량 상위 N개 상품 조회 - 해당 일자에 상품 없음`() {
        val limit = 3
        val startDate = LocalDate.now().minusDays(3)
        val endDate = LocalDate.now()

        val result = repository.findTopNProductsByQuantity(startDate, endDate, limit)

        assertThat(result).isEmpty()
    }
}
