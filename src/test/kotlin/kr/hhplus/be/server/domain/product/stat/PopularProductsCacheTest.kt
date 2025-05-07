package kr.hhplus.be.server.domain.product.stat

import kr.hhplus.be.server.common.cache.CacheNames
import kr.hhplus.be.server.common.util.TimeZone
import kr.hhplus.be.server.domain.product.repository.ProductDailySaleStatRepository
import kr.hhplus.be.server.domain.product.stat.command.CreateProductDailySaleStatsCommand
import kr.hhplus.be.server.testutil.DatabaseTestHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import java.time.LocalDate

@SpringBootTest
@Isolated
class PopularProductsCacheTest {

    @Autowired
    private lateinit var productSaleStatService: ProductSaleStatService

    @MockitoSpyBean
    private lateinit var dailyStatRepository: ProductDailySaleStatRepository

    @Autowired
    private lateinit var cacheManager: CacheManager

    @Autowired
    private lateinit var databaseTestHelper: DatabaseTestHelper

    @BeforeEach
    fun setup() {
        databaseTestHelper.clearProductDailySaleStat()
        cacheManager.getCache(CacheNames.POPULAR_PRODUCTS)?.clear()
    }

    @Test
    @DisplayName("인기 상품 ID 조회 결과가 캐시에 저장되어야 한다")
    fun saveCache() {
        databaseTestHelper.savedProductDailySaleStat(date = LocalDate.now(TimeZone.KSTId).minusDays(1))
        databaseTestHelper.savedProductDailySaleStat(date = LocalDate.now(TimeZone.KSTId).minusDays(1))
        databaseTestHelper.savedProductDailySaleStat(date = LocalDate.now(TimeZone.KSTId).minusDays(2))

        val firstCall = productSaleStatService.getPopularProductIds()
        val secondCall = productSaleStatService.getPopularProductIds()

        assertThat(firstCall).isEqualTo(secondCall)
        Mockito.verify(
            dailyStatRepository,
            times(1)
        ).findTopNProductsByQuantity(
            startDate = PopularProductsIds.getStartDay(),
            endDate = PopularProductsIds.getEndDay(),
            limit = PopularProductsIds.MAX_SIZE
        )
    }

    @Test
    @DisplayName("인기 상품 ID 조회 결과가 비어있으면 캐시에 저장하지 않는다")
    fun getPopularProductIds_shouldCacheResult() {
        val firstCall = productSaleStatService.getPopularProductIds()
        val secondCall = productSaleStatService.getPopularProductIds()

        assertThat(firstCall.value).isEmpty()
        assertThat(secondCall.value).isEmpty()
        Mockito.verify(
            dailyStatRepository,
            times(2)
        ).findTopNProductsByQuantity(
            startDate = PopularProductsIds.getStartDay(),
            endDate = PopularProductsIds.getEndDay(),
            limit = PopularProductsIds.MAX_SIZE
        )
    }

    @Test
    @DisplayName("일별 통계 생성 시 인기 상품 캐시가 제거되어야 한다")
    fun createDailyStats_shouldEvictCache() {
        databaseTestHelper.savedProductDailySaleStat(date = LocalDate.now(TimeZone.KSTId).minusDays(1))
        databaseTestHelper.savedProductDailySaleStat(date = LocalDate.now(TimeZone.KSTId).minusDays(1))
        databaseTestHelper.savedProductDailySaleStat(date = LocalDate.now(TimeZone.KSTId).minusDays(2))

        productSaleStatService.getPopularProductIds()
        productSaleStatService.createDailyStats(CreateProductDailySaleStatsCommand(LocalDate.now()))
        productSaleStatService.getPopularProductIds()

        Mockito.verify(
            dailyStatRepository,
            times(2)
        ).findTopNProductsByQuantity(
            startDate = PopularProductsIds.getStartDay(),
            endDate = PopularProductsIds.getEndDay(),
            limit = PopularProductsIds.MAX_SIZE
        )
    }
}
