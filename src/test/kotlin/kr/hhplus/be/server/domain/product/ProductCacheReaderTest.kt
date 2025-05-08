package kr.hhplus.be.server.domain.product

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.testutil.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProductCacheReaderTest {
    private lateinit var reader: ProductCacheReader
    private val repository = mockk<ProductRepository>(relaxed = true)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        reader = ProductCacheReader(repository)
    }

    @Nested
    @DisplayName("단일 상품 조회")
    inner class GetProduct {
        @Test
        @DisplayName("ID로 상품을 조회하여 반환한다")
        fun getProductById() {
            val id = ProductMock.id()
            val product = ProductMock.product(id = id)
            every { repository.findById(id.value) } returns product

            val result = reader.getOrNull(id.value)

            assertThat(result?.id).isEqualTo(id)
            verify { repository.findById(id.value) }
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 null이 반환된다")
        fun throwsExceptionForNonExistentProduct() {
            val id = ProductMock.id()
            every { repository.findById(id.value) } returns null

            val result = reader.getOrNull(id.value)

            assertThat(result).isNull()
        }
    }
}
