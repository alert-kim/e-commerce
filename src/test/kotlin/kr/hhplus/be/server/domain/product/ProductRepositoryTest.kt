package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.testutil.assertion.ProductAssert.Companion.assertProduct
import kr.hhplus.be.server.testutil.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import java.time.Instant

@Import(ProductRepositoryTestConfig::class)
@Isolated
class ProductRepositoryTest @Autowired constructor(
    private val productRepository: ProductRepository,
    private val testProductRepository: TestProductRepository
) : RepositoryTest() {

    @BeforeEach
    fun setup() {
        testProductRepository.deleteAll()
    }

    @Nested
    @DisplayName("ID로 조회")
    inner class FindById {

        @Test
        @DisplayName("해당 상품 반환")
        fun findById() {
            val product = testProductRepository.save(ProductMock.product(id = null))

            val result = productRepository.findById(product.id().value)

            assertThat(result).isNotNull
            result?.let {
                assertProduct(it).isEqualTo(product)
            }
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 null 반환")
        fun findNonExisting() {
            val nonExistingId = ProductMock.id().value

            val result = productRepository.findById(nonExistingId)

            assertThat(result).isNull()
        }
    }

    @Nested
    @DisplayName("ID 목록으로 상품 조회")
    inner class FindAllByIds {

        @Test
        @DisplayName("해당 상품 반환")
        fun findAllByIds() {
            val expected = List(3) {
                testProductRepository.save(ProductMock.product(id = null))
            }
            testProductRepository.save(ProductMock.product(id = null))
            val ids = expected.map { it.id().value }

            val result = productRepository.findAllByIds(ids)

            assertThat(result).hasSize(expected.size)
            result.forEach { product ->
                val target = expected.first { it.id() == product.id() }
                assertProduct(product).isEqualTo(target)
            }
        }

        @Test
        @DisplayName("상품이 없는 경우 빈 리스트 반환")
        fun findAllByIdsNotExists() {
            val ids = List(3) { ProductMock.id().value }

            val result = productRepository.findAllByIds(ids)

            assertThat(result).isEmpty()
        }
    }

    @Nested
    @DisplayName("상태별 상품 목록 페이징 조회")
    inner class FindAllByStatus {

        @Test
        @DisplayName("해당 상태를 가진 상품을 페이지로 반환")
        fun findAllByStatusInPage() {
            val status = ProductStatus.ON_SALE
            val pageable = PageRequest.of(0, 5)
            val products = List(pageable.pageSize) {
                testProductRepository.save(
                    ProductMock.product(
                        id = null,
                        status = status,
                        createdAt = Instant.now().minusSeconds(it.toLong() * 1000)
                    )
                )
            }
            testProductRepository.save(
                ProductMock.product(
                    id = null,
                    status = ProductStatus.INACTIVE
                )
            )

            val result = productRepository.findAllByStatusOrderByCreatedAtDesc(status, pageable)

            assertThat(result.totalElements).isEqualTo(products.size.toLong())
            assertThat(result.totalPages).isEqualTo(1)
            assertThat(result.content.size).isEqualTo(products.size)
            result.content.forEachIndexed { index, product ->
                val target = products[index]
                assertProduct(product).isEqualTo(target)
            }
        }

        @Test
        @DisplayName("해당 상태를 가진 상품이 없을 경우 빈 페이지 반환")
        fun findAllByStatusInPageNotExists() {
            val status = ProductStatus.ON_SALE
            val pageable = PageRequest.of(0, 5)

            val result = productRepository.findAllByStatusOrderByCreatedAtDesc(status, pageable)

            assertThat(result.totalElements).isEqualTo(0)
            assertThat(result.totalPages).isEqualTo(0)
            assertThat(result.content).isEmpty()
        }

        @Test
        @DisplayName("총 개수가 요청 수보다 많은 경우 알맞게 페이징 하여 반환")
        fun pagingIfTotalIsMoreThanRequest() {
            val status = ProductStatus.ON_SALE
            val pageable = PageRequest.of(0, 5)
            val products = List(10) {
                testProductRepository.save(
                    ProductMock.product(
                        id = null,
                        status = status,
                        createdAt = Instant.now().minusSeconds(it.toLong() * 1000)
                    )
                )
            }

            val result = productRepository.findAllByStatusOrderByCreatedAtDesc(status, pageable)

            assertThat(result.totalElements).isEqualTo(products.size.toLong())
            assertThat(result.totalPages).isEqualTo(2)
            assertThat(result.content.size).isEqualTo(5)
            result.content.forEachIndexed { index, product ->
                val target = products[index]
                assertProduct(product).isEqualTo(target)
            }
        }

        @Test
        @DisplayName("요청한 페이지에 들어갈 상품이, 요청 페이지 사이즈 보다 적은 경우 해당 상품만 반환")
        fun returnOnlyAvailableIfRequestedMore() {
            val status = ProductStatus.ON_SALE
            val pageable = PageRequest.of(3, 3)
            val products = List(10) {
                testProductRepository.save(
                    ProductMock.product(
                        id = null,
                        status = status,
                        createdAt = Instant.now().minusSeconds(it.toLong() * 1000)
                    )
                )
            }

            val result = productRepository.findAllByStatusOrderByCreatedAtDesc(status, pageable)

            assertThat(result.totalElements).isEqualTo(products.size.toLong())
            assertThat(result.totalPages).isEqualTo(4)
            assertThat(result.content.size).isEqualTo(1)
            result.content.first().also { product ->
                val target = products.last()
                assertProduct(product).isEqualTo(target)
            }
        }

        @Test
        @DisplayName("요청한 페이지를 채울 상품이 없는 경우, 빈 페이지 반환")
        fun emptyPageIfNotProducts() {
            val status = ProductStatus.ON_SALE
            val pageable = PageRequest.of(1, 5) // 요청 페이지 1, 페이지 크기 5
            val products = List(3) {
                testProductRepository.save(ProductMock.product(id = null, status = status))
            }

            val result = productRepository.findAllByStatusOrderByCreatedAtDesc(status, pageable)

            assertThat(result.totalElements).isEqualTo(products.size.toLong())
            assertThat(result.content).isEmpty()
            assertThat(result.totalPages).isEqualTo(1)
        }
    }
}
