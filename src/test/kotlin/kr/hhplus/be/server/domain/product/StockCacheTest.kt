package kr.hhplus.be.server.domain.stock

import kr.hhplus.be.server.common.cache.CacheNames
import kr.hhplus.be.server.domain.stock.command.AllocateStockCommand
import kr.hhplus.be.server.domain.stock.command.RestoreStockCommand
import kr.hhplus.be.server.testutil.DatabaseTestHelper
import kr.hhplus.be.server.testutil.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean

@SpringBootTest
@Isolated
class StockCacheTest {

    @Autowired
    private lateinit var reader: StockCacheReader

    @Autowired
    private lateinit var service: StockService

    @MockitoSpyBean
    private lateinit var repository: StockRepository

    @Autowired
    private lateinit var cacheManager: CacheManager

    @Autowired
    private lateinit var databaseTestHelper: DatabaseTestHelper

    @BeforeEach
    fun setup() {
        cacheManager.getCache(CacheNames.STOCK_BY_PRODUCT)?.clear()
    }

    @Nested
    @DisplayName("캐시 저장")
    inner class Save {
        @Test
        @DisplayName("재고 조회 결과를 캐시에 저장")
        fun saveCache() {
            val stock = databaseTestHelper.savedStock()
            val productId = stock.productId

            reader.getOrNullByProductId(productId)
            reader.getOrNullByProductId(productId)

            Mockito.verify(
                repository,
                times(1)
            ).findByProductId(
                productId
            )
        }

        @Test
        @DisplayName("존재하지 않는 재고 조회 시 null이 반환되고 캐시에 저장되지 않는다")
        fun doNotCacheNullResult() {
            val productId = ProductMock.id()

            val firstCall = reader.getOrNullByProductId(productId)
            val secondCall = reader.getOrNullByProductId(productId)

            assertThat(firstCall).isNull()
            assertThat(secondCall).isNull()
            Mockito.verify(
                repository,
                times(2)
            ).findByProductId(
                productId
            )
        }
    }

    @Nested
    @DisplayName("캐시 제거")
    inner class Evict {
        @Test
        @DisplayName("재고 할당 시 캐시가 제거되어야 한다")
        fun allocateStockShouldEvictCache() {
            val stock = databaseTestHelper.savedStock()
            val productId = stock.productId
            reader.getOrNullByProductId(productId) // 캐시 저장

            service.allocate(AllocateStockCommand(productId = productId, quantity = 1)) // 캐시 삭제
            reader.getOrNullByProductId(productId) // 캐시에 없으므로 repository 호출

            Mockito.verify(
                repository,
                times(2)
            ).findByProductId(
                productId
            )
        }

        @Test
        @DisplayName("재고 복구 시 캐시가 제거되어야 한다")
        fun restoreStockShouldEvictCache() {
            val stock = databaseTestHelper.savedStock()
            val productId = stock.productId
            reader.getOrNullByProductId(productId) // 캐시 저장

            service.restore(RestoreStockCommand(productId = productId, quantity = 1)) // 캐시 삭제
            reader.getOrNullByProductId(productId) // 캐시에 없으므로 repository 호출

            Mockito.verify(
                repository,
                times(2)
            ).findByProductId(
                productId
            )
        }
    }
}
