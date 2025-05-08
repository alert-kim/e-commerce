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
class ProductCacheTest {

    @Autowired
    private lateinit var reader: ProductCacheReader

    @MockitoSpyBean
    private lateinit var repository: ProductRepository

    @Autowired
    private lateinit var cacheManager: CacheManager

    @Autowired
    private lateinit var databaseTestHelper: DatabaseTestHelper

    @BeforeEach
    fun setUp() {
        cacheManager.getCache(CacheNames.PRODUCT)?.clear()
    }

    @Nested
    @DisplayName("단일 상품 조회에 대한 캐시 적용")
    inner class Cache {

        @Test
        @DisplayName("상품 조회 결과가 캐시에 저장되어야 한다")
        fun saveCache() {
            val product = databaseTestHelper.savedProduct()
            val productId = product.id().value

            reader.getOrNull(productId)
            reader.getOrNull(productId)

            Mockito.verify(
                repository,
                times(1)
            ).findById(
                productId
            )
        }


        @Test
        @DisplayName("존재하지 않는 상품 조회 시 null이 반환되고 캐시에 저장되지 않는다")
        fun doNotCacheNullResult() {
            val productId = ProductMock.id()

            val firstCall = reader.getOrNull(productId.value)
            val secondCall = reader.getOrNull(productId.value)

            assertThat(firstCall).isNull()
            assertThat(secondCall).isNull()
            Mockito.verify(
                repository,
                times(2)
            ).findById(
                productId.value
            )
        }
    }
}
