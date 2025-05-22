package kr.hhplus.be.server.domain.product.stat

import kr.hhplus.be.server.domain.product.repository.ProductDailySaleStatRepository
import kr.hhplus.be.server.domain.product.stat.repository.ProductSaleStatRepository
import kr.hhplus.be.server.testutil.mock.ProductMock
import org.assertj.core.api.Assertions
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
class ProductDailySaleStatRepositoryTest @Autowired constructor(
    private val repository: ProductDailySaleStatRepository,
    private val testRepository: TestProductDailySaleStatRepository,
    private val productSaleStatRepository: ProductSaleStatRepository
) {

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

            val result = testRepository.findAll()
            val dailySale1 = result.firstOrNull { it.productId == productId1 }
            Assertions.assertThat(dailySale1).isNotNull
            Assertions.assertThat(dailySale1?.date).isEqualTo(today)
            Assertions.assertThat(dailySale1?.quantity).isEqualTo(5)
            val dailySale2 = result.firstOrNull { it.productId == productId2 }
            Assertions.assertThat(dailySale2).isNotNull
            Assertions.assertThat(dailySale2?.date).isEqualTo(today)
            Assertions.assertThat(dailySale2?.quantity).isEqualTo(4)
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

            val result = testRepository.findAll()
                .firstOrNull { it.productId == productId }
            Assertions.assertThat(result).isNotNull
            Assertions.assertThat(result?.date).isEqualTo(today)
            Assertions.assertThat(result?.quantity).isEqualTo(10)
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

            val result = testRepository.findAll()
                .firstOrNull { it.productId == productId }
            Assertions.assertThat(result).isNotNull
            Assertions.assertThat(result?.date).isEqualTo(today)
            Assertions.assertThat(result?.quantity).isEqualTo(3)
        }
    }
}
