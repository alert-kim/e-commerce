package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.common.cache.CacheNames
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
class ProductCacheTest @Autowired constructor(
    private val reader: ProductCacheReader,
    private val cacheManager: CacheManager,
    private val databaseTestHelper: DatabaseTestHelper,
) {

    @MockitoSpyBean
    private lateinit var repository: ProductRepository

    @BeforeEach
    fun setUp() {
        cacheManager.getCache(CacheNames.PRODUCT)?.clear()
    }

    @Nested
    @DisplayName("캐시 저장")
    inner class Save {

        @Test
        @DisplayName("상품 조회 결과를 캐시에 저장")
        fun saveCache() {
            val product = databaseTestHelper.savedProduct()
            val productId = product.id().value

            reader.getOrNull(productId)
            reader.getOrNull(productId)

            Mockito.verify(
                repository,
                times(1)
            ).findById(productId)
        }

        @Test
        @DisplayName("상품이 존재하지 않으면 캐싱하지 않음")
        fun doNotCacheNullResult() {
            val productId = ProductMock.id()

            val firstCall = reader.getOrNull(productId.value)
            val secondCall = reader.getOrNull(productId.value)

            assertThat(firstCall).isNull()
            assertThat(secondCall).isNull()
            Mockito.verify(
                repository,
                times(2)
            ).findById(productId.value)
        }
    }
}
