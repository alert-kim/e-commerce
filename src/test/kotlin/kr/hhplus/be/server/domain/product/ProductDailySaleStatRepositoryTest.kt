package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.product.repository.ProductDailySaleStatRepository
import kr.hhplus.be.server.domain.product.stat.ProductDailySaleStat
import kr.hhplus.be.server.domain.product.stat.ProductSaleStat
import kr.hhplus.be.server.domain.product.stat.repository.ProductSaleStatRepository
import kr.hhplus.be.server.testutil.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.time.Instant
import java.time.LocalDate

@Isolated
@Import(ProductDailySaleStatRepositoryTestConfig::class)
@SpringBootTest
class ProductDailySaleStatRepositoryTest {
    @Autowired
    lateinit var repository: ProductDailySaleStatRepository

    @Autowired
    lateinit var testRepository: TestProductDailySaleStatRepository

    @Autowired
    lateinit var productSaleStatRepository: ProductSaleStatRepository

    @BeforeEach
    fun setup() {
        testRepository.deleteAll()
    }

    @Nested
    @DisplayName("일별 판매 통계 집계")
    inner class AggregateDailyStatsByDate {
        
        @Test
        @DisplayName("특정 날짜의 판매 통계를 집계하여 일별 통계 테이블에 저장한다")
        fun aggregateDailyStatsByDate() {
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
        @DisplayName("이미 집계된 날짜에 새 데이터가 추가되면 통계를 업데이트한다")
        fun updateExistingStats() {
            val today = LocalDate.now()
            val productId = ProductMock.id()
            val stat = ProductDailySaleStat(
                id = null,
                date = today,
                productId = productId,
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
        @DisplayName("이전 날짜의 판매 통계는 반영하지 않는다")
        fun excludePreviousDaysStats() {
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
    }

    @Nested
    @DisplayName("판매량 상위 N개 상품 조회")
    inner class FindTopNProductsByQuantity {

        @Test
        @DisplayName("판매량 기준으로 상위 N개 상품을 조회한다 - limit 보다 상품 수가 많은 경우")
        fun moreProductsExist() {
            val limit = 3
            val startDate = LocalDate.now().minusDays(3)
            val endDate = LocalDate.now()
            val productsInOrderBySale = List(limit + 1) {
                testRepository.save(ProductMock.dailySale(id = null, date = startDate, quantity = 10 - it))
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
        @DisplayName("판매량 기준으로 상위 N개 상품을 조회한다 - limit 보다 상품 수가 적은 경우")
        fun lessThanLimit() {
            val limit = 3
            val startDate = LocalDate.now().minusDays(3)
            val endDate = LocalDate.now()
            val productsInOrderBySale = List(limit - 1) {
                testRepository.save(ProductMock.dailySale(id = null, date = startDate, quantity = 10 - it))
            }

            val result = repository.findTopNProductsByQuantity(startDate, endDate, limit)

            assertThat(result).hasSize(productsInOrderBySale.size)
        }

        @Test
        @DisplayName("기준일 범위 외의 날짜 데이터는 조회하지 않는다")
        fun shouldExcludeProductsOutsideDateRange() {
            val limit = 4
            val startDate = LocalDate.now().minusDays(3)
            val endDate = LocalDate.now()
            testRepository.save(ProductMock.dailySale(id = null, date = startDate.minusDays(1), quantity = 10))
            testRepository.save(ProductMock.dailySale(id = null, date = endDate.plusDays(1), quantity = 10))

            val result = repository.findTopNProductsByQuantity(startDate, endDate, limit)

            assertThat(result).isEmpty()
        }

        @Test
        @DisplayName("해당 일자에 판매된 상품이 없으면 빈 목록을 반환한다")
        fun shouldReturnEmptyListWhenNoProductsExist() {
            val limit = 3
            val startDate = LocalDate.now().minusDays(3)
            val endDate = LocalDate.now()

            val result = repository.findTopNProductsByQuantity(startDate, endDate, limit)

            assertThat(result).isEmpty()
        }
    }
    
    @Nested
    @DisplayName("판매량 상위 N개 상품 ID 조회")
    inner class FindTopNProductIdsByQuantity {
        
        @Test
        @DisplayName("판매량 기준으로 상위 N개 상품 ID를 조회")
        fun topNProductIds() {
            val limit = 3
            val startDate = LocalDate.now().minusDays(4)
            val endDate = LocalDate.now().minusDays(1)
            val productsInOrderBySale = List(3) {
                testRepository.save(ProductMock.dailySale(id = null, date = startDate.plusDays(it.toLong()), quantity = 20 - it * 2))
            }
            testRepository.save(ProductMock.dailySale(id = null, date = startDate.minusDays(1), quantity = 30))
            testRepository.save(ProductMock.dailySale(id = null, date = endDate.plusDays(1), quantity = 30))

            val result = repository.findTopNProductIdsByQuantity(startDate, endDate, limit)
            
            assertThat(result).hasSize(limit)
            val expectedIds = productsInOrderBySale.take(limit).map { it.productId }
            assertThat(result).isEqualTo(expectedIds)
        }
        
        @Test
        @DisplayName("해당 기간에 판매된 상품이 없으면 빈 목록을 반환한다")
        fun shouldReturnEmptyListWhenNoSales() {
            val limit = 5
            val startDate = LocalDate.now().minusDays(3)
            val endDate = LocalDate.now()
            testRepository.save(ProductMock.dailySale(id = null, date = startDate.minusDays(1), quantity = 10))
            testRepository.save(ProductMock.dailySale(id = null, date = endDate.plusDays(1), quantity = 15))
            
            val result = repository.findTopNProductIdsByQuantity(startDate, endDate, limit)
            
            assertThat(result).isEmpty()
        }
    }
}
