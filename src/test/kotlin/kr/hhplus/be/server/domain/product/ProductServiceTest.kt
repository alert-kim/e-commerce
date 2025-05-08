package kr.hhplus.be.server.domain.product


import io.kotest.assertions.throwables.shouldThrow
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.common.InvalidPageRequestArgumentException
import kr.hhplus.be.server.domain.product.excpetion.NotFoundProductException
import kr.hhplus.be.server.testutil.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

class ProductServiceTest {
    private lateinit var service: ProductService
    private val cachedReader = mockk<ProductCacheReader>(relaxed = true)
    private val repository = mockk<ProductRepository>(relaxed = true)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        service = ProductService(cachedReader, repository)
    }

    @Nested
    @DisplayName("상태별 상품 목록 페이징 조회")
    inner class GetAllByStatusPaged {

        @Test
        @DisplayName("해당 상태의 상품 목록을 페이징해서 조회 모델로 반환")
        fun productsPagedByStatus() {
            val status = ProductStatus.entries.random()
            val page = 0
            val pageSize = 10
            val pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
            val products = List(pageSize) {
                ProductMock.product()
            }
            val totalCount = products.size * 2L
            val productPage = PageImpl(products, pageable, totalCount)
            every { repository.findAllByStatusOrderByCreatedAtDesc(status, pageable) } returns productPage

            val result = service.getAllByStatusOnPaged(status, page, pageSize)

            assertThat(result.pageable).isEqualTo(pageable)
            assertThat(result.totalElements).isEqualTo(totalCount)
            assertThat(result.content).hasSize(products.size)
            result.content.forEachIndexed { index, product ->
                assertThat(product.id).isEqualTo(products[index].id())
                assertThat(product.status).isEqualTo(products[index].status)
                assertThat(product.name).isEqualTo(products[index].name)
                assertThat(product.description).isEqualTo(products[index].description)
                assertThat(product.price.value).isEqualByComparingTo(products[index].price)
                assertThat(product.createdAt).isEqualTo(products[index].createdAt)
            }
            verify {
                repository.findAllByStatusOrderByCreatedAtDesc(status, pageable)
            }
        }

        @Test
        @DisplayName("해당 상품이 없는 경우 빈 페이지 반환")
        fun emptyPage() {
            val status = ProductStatus.ON_SALE
            val page = 0
            val pageSize = 10
            val productPage = PageImpl(emptyList<Product>())
            every { repository.findAllByStatusOrderByCreatedAtDesc(status, any()) } returns productPage

            val result = service.getAllByStatusOnPaged(status, page, pageSize)

            assertThat(result.content).isEmpty()
        }

        @Test
        @DisplayName("유효하지 않은 페이징 요청 값인 경우 InvalidPageRequestArgumentException 발생")
        fun invalidPageRequest() {
            val status = ProductStatus.ON_SALE
            val page = 0
            val pageSize = 0

            shouldThrow<InvalidPageRequestArgumentException> {
                service.getAllByStatusOnPaged(status, page, pageSize)
            }

            verify(exactly = 0) {
                repository.findAllByStatusOrderByCreatedAtDesc(any(), any())
            }
        }
    }

    @Nested
    @DisplayName("ID로 상품 조회")
    inner class GetById {
        @Test
        @DisplayName("주어진 ID에 해당하는 상품 반환")
        fun getProduct() {
            val id = ProductMock.id()
            val product = ProductMock.view(id = id)
            every { cachedReader.getOrNull(id.value) } returns product

            val result = service.get(id.value)

            assertThat(result.id).isEqualTo(id)
            verify {
                cachedReader.getOrNull(id.value)
            }
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 NotFoundProductException이 발생한다")
        fun throwsExceptionForNonExistentProduct() {
            val id = ProductMock.id()
            every { cachedReader.getOrNull(id.value) } returns null

            shouldThrow<NotFoundProductException> {
                service.get(id.value)
            }
            verify { cachedReader.getOrNull(id.value) }
        }
    }

    @Nested
    @DisplayName("ID로 상품 목록 조회")
    inner class GetAllByIds {
        @Test
        @DisplayName("주어진 ID 목록에 해당하는 상품 반환")
        fun getAllProduct() {
            val ids = listOf(1L, 2L, 3L)
            val products = ids.map { ProductMock.view(id = ProductId(it)) }
            products.forEach {
                every { cachedReader.getOrNull(it.id.value) } returns it
            }

            val result = service.getAllByIds(ids)

            assertThat(result.value).hasSize(products.size)
            result.value.forEachIndexed { index, product ->
                assertThat(product.id).isEqualTo(products[index].id)
            }
            verify {
                ids.forEach { cachedReader.getOrNull(it) }
            }
        }

        @Test
        @DisplayName("주어진 ID 목록에 해당하는 상품이 없으면 빈 리스트 반환")
        fun emptyList() {
            val ids = listOf(1L, 2L, 3L)
            every { cachedReader.getOrNull(any()) } returns null

            val result = service.getAllByIds(ids)

            assertThat(result.value).isEmpty()
        }
    }
}
