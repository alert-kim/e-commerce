package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.testutil.mock.ProductMock
import kr.hhplus.be.server.testutil.assertion.ProductAssert.Companion.assertProduct
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import java.time.Instant

@Import(ProductRepositoryTestConfig::class)
class ProductRepositoryTest : RepositoryTest() {

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var testProductRepository: TestProductRepository

    @Test
    fun `findAllByIds - 해당 상품 반환`() {
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
    fun `findAllByIds - 상품이 없는 경우 빈 리스트 반환`() {
        val ids = List(3) { ProductMock.id().value }

        val result = productRepository.findAllByIds(ids)

        assertThat(result).isEmpty()
    }


    @Test
    fun `findAllByStatus - 해당 상태를 가진 상품을 페이지로 반환`() {
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
    fun `findAllByStatus - 해당 상태를 가진 상품이 없을 경우 빈 페이지 반환`() {
        val status = ProductStatus.ON_SALE
        val pageable = PageRequest.of(0, 5)


        val result = productRepository.findAllByStatusOrderByCreatedAtDesc(status, pageable)

        assertThat(result.totalElements).isEqualTo(0)
        assertThat(result.totalPages).isEqualTo(0)
        assertThat(result.content).isEmpty()
    }

    @Test
    fun `findAllByStatus - 총 개수가 요청 수보다 많은 경우 알맞게 페이징 하여 반환`() {
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
    fun `findAllByStatus - 요청한 페이지에 들어갈 상품이, 요청 페이지 사이즈 보다 적은 경우 해당 상품만 반환`() {
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
    fun `findAllByStatus - 요청한 페이지를 채울 상품이 없는 경우, 빈 페이지 반환`() {
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


//    @Test
//    fun `상태별로 상품을 페이징 조회한다`() {
//        // given
//        for (i in 1..10) {
//            val product = ProductMock.product(
//                id = null,
//                name = "액티브 상품$i",
//                status = ProductStatus.ACTIVE
//            )
//            productRepository.save(product)
//        }
//
//        for (i in 1..5) {
//            val product = ProductMock.product(
//                id = null,
//                name = "비활성 상품$i",
//                status = ProductStatus.INACTIVE
//            )
//            productRepository.save(product)
//        }
//
//        val pageable = PageRequest.of(0, 5, Sort.by("name"))
//
//        // when
//        val activeProductPage = productRepository.findAllByStatus(ProductStatus.ACTIVE, pageable)
//        val inactiveProductPage = productRepository.findAllByStatus(ProductStatus.INACTIVE, pageable)
//
//        // then
//        assertThat(activeProductPage.totalElements).isEqualTo(10)
//        assertThat(activeProductPage.content).hasSize(5)
//        assertThat(activeProductPage.totalPages).isEqualTo(2)
//
//        assertThat(inactiveProductPage.totalElements).isEqualTo(5)
//        assertThat(inactiveProductPage.content).hasSize(5)
//        assertThat(inactiveProductPage.totalPages).isEqualTo(1)
//
//        // 이름 정렬 확인
//        val activeProductNames = activeProductPage.content.map { it.name }
//        assertThat(activeProductNames).isSorted()
//    }
//
//    @Test
//    fun `상품 상태를 변경할 수 있다`() {
//        // given
//        val product = ProductMock.product(
//            id = null,
//            name = "상태 변경 상품",
//            status = ProductStatus.ACTIVE
//        )
//        productRepository.save(product)
//        val productId = product.requireId().value
//
//        // when
//        product.deactivate()
//        productRepository.save(product)
//
//        // then
//        val foundProduct = productRepository.findAllByIds(listOf(productId)).firstOrNull()
//        assertProduct(foundProduct)
//            .isNotNull
//            .hasStatus(ProductStatus.INACTIVE)
//
//        // 다시 활성화
//        foundProduct!!.activate()
//        productRepository.save(foundProduct)
//
//        val updatedProduct = productRepository.findAllByIds(listOf(productId)).firstOrNull()
//        assertProduct(updatedProduct)
//            .isNotNull
//            .hasStatus(ProductStatus.ACTIVE)
//    }
//
//    @Test
//    fun `존재하지 않는 ID로 조회하면 빈 목록을 반환한다`() {
//        // given
//        val nonExistingId = 9999L
//
//        // when
//        val foundProducts = productRepository.findAllByIds(listOf(nonExistingId))
//
//        // then
//        assertThat(foundProducts).isEmpty()
//    }
}
