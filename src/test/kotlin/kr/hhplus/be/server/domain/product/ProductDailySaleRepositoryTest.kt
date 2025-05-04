package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.domain.product.repository.ProductDailySaleRepository
import kr.hhplus.be.server.domain.product.stat.ProductSaleStat
import kr.hhplus.be.server.domain.product.stat.repository.ProductSaleStatRepository
import kr.hhplus.be.server.testutil.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.time.Instant
import java.time.LocalDate

@Isolated
@Import(ProductDailySaleRepositoryTestConfig::class)
@SpringBootTest
class ProductDailySaleRepositoryTest {
    @Autowired
    lateinit var repository: ProductDailySaleRepository

    @Autowired
    lateinit var testRepository: TestProductDailySaleRepository

    @Autowired
    lateinit var productSaleStatRepository: ProductSaleStatRepository

    @BeforeEach
    fun setup() {
        testRepository.deleteAll()
    }

    @Test
    fun `save - 판매 데이터 저장`() {
        val sale = ProductMock.dailySale()

        val saved = repository.save(sale)

        assertThat(saved.productId).isEqualTo(sale.productId)
        assertThat(saved.date).isEqualTo(sale.date)
        assertThat(saved.quantity).isEqualTo(sale.quantity)
    }

    @Test
    @DisplayName("aggregateDailyStatsByDate - 특정 날짜의 판매 통계를 집계하여 일별 통계 테이블에 저장한다")
    fun aggregateDailyStat() {
        val today = LocalDate.now()
        val productId1 = ProductMock.id()
        val productId2 = ProductMock.id()
        val stat1 = ProductSaleStat(null, productId1, 3, today, Instant.now())
        val stat2 = ProductSaleStat(null, productId1, 2, today, Instant.now())
        val stat3 = ProductSaleStat(null, productId2, 4, today, Instant.now())
        productSaleStatRepository.save(stat1)
        productSaleStatRepository.save(stat2)
        productSaleStatRepository.save(stat3)

        repository.aggregateDailyStatsByDate(today)

        val result = repository.findTopNProductsByQuantity(today, today, 10)
        val dailySale1 = result.firstOrNull { it.productId == productId1 }
        assertThat(dailySale1).isNotNull
        assertThat(dailySale1?.date).isEqualTo(today)
        assertThat(dailySale1?.quantity).isEqualTo(5)
        val dailySale2 = result.firstOrNull { it.productId == productId2 }
        assertThat(dailySale2).isNotNull
        assertThat(dailySale2?.date).isEqualTo(today)
        assertThat(dailySale2?.quantity).isEqualTo(4)
    }

    @Test
    @DisplayName("aggregateDailyStatsByDate - 이미 집계된 날짜에 새 데이터가 추가되면 통계를 업데이트한다")
    fun aggregateDailyStatsByDate_updatesExistingStats() {
        val today = LocalDate.now()
        val productId = ProductMock.id()
        val stat = ProductDailySale(
            id = ProductDailySaleId(today, productId),
            quantity = 1,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
        testRepository.save(stat)

        listOf(
            ProductSaleStat(null, productId, 2, today, Instant.now()),
            ProductSaleStat(null, productId, 4, today, Instant.now()),
            ProductSaleStat(null, productId, 4, today, Instant.now()),
        ).forEach {
            productSaleStatRepository.save(it)
        }
        repository.aggregateDailyStatsByDate(today)

        val result = repository.findTopNProductsByQuantity(today, today, 10)
            .firstOrNull { it.productId == productId }
        assertThat(result).isNotNull
        assertThat(result?.date).isEqualTo(today)
        assertThat(result?.quantity).isEqualTo(10)
    }

    @Test
    @DisplayName("aggregateDailyStatsByDate - 이전 날짜의 판매 통계는 반영하지 않는다")
    fun aggregateDailyStatsByDate_aggregatesAndSavesStats() {
        val today = LocalDate.now()
        val productId = ProductMock.id()
        val todayStat = ProductSaleStat(null, productId, 3, today, Instant.now())
        val yesterdayStat = ProductSaleStat(null, productId, 2, today.minusDays(1), Instant.now())
        productSaleStatRepository.save(todayStat)
        productSaleStatRepository.save(yesterdayStat)

        repository.aggregateDailyStatsByDate(today)

        val result = repository.findTopNProductsByQuantity(today, today, 10)
            .firstOrNull { it.productId == productId }
        assertThat(result).isNotNull
        assertThat(result?.date).isEqualTo(today)
        assertThat(result?.quantity).isEqualTo(3)
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
