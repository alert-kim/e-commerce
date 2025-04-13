package kr.hhplus.be.server.application.product

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.domain.common.InvalidPageRequestArgumentException
import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.product.ProductStatus
import kr.hhplus.be.server.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@ExtendWith(MockKExtension::class)
class DeductionItemFacadeTest {
    @InjectMockKs
    private lateinit var facade: ProductFacade

    @MockK(relaxed = true)
    private lateinit var service: ProductService

    @BeforeEach
    fun setUp() {
        clearMocks(service)
    }

    @Test
    fun `getAllOnSalePaged - 판매 중인 상품 목록을 페이징해서 조회 모델로 반환`() {
        val page = 0
        val pageSize = 10
        val pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        val products = List(pageSize) {
            ProductMock.product()
        }
        val totalCount = products.size * 2L
        val productPage = PageImpl(products, pageable, totalCount)
        every { service.getAllByStatusOnPaged(status = ProductStatus.ON_SALE, page = page, pageSize = pageSize) } returns productPage

        val result = facade.getAllOnSalePaged(page, pageSize)

        assertThat(result.pageable).isEqualTo(pageable)
        assertThat(result.totalElements).isEqualTo(totalCount)
        assertThat(result.content).hasSize(products.size)
        result.content.forEachIndexed { index, productQueryModel ->
            assertThat(productQueryModel.id).isEqualTo(products[index].id)
            assertThat(productQueryModel.status).isEqualTo(products[index].status)
            assertThat(productQueryModel.name).isEqualTo(products[index].name)
            assertThat(productQueryModel.description).isEqualTo(products[index].description)
            assertThat(productQueryModel.price).isEqualByComparingTo(products[index].price)
            assertThat(productQueryModel.stock).isEqualByComparingTo(products[index].stock.quantity)
            assertThat(productQueryModel.createdAt).isEqualTo(products[index].createdAt)
        }
        verify {
            service.getAllByStatusOnPaged(status = ProductStatus.ON_SALE, page = page, pageSize = pageSize)
        }
    }

    @Test
    fun `getAllOnSalePaged - 판매 중인 상품이 없는 경우 빈 페이지 반환`() {
        val page = 0
        val pageSize = 10
        val productPage = PageImpl(emptyList<Product>())
        every { service.getAllByStatusOnPaged(ProductStatus.ON_SALE, any(), any()) } returns productPage

        val result = facade.getAllOnSalePaged(page, pageSize)

        assertThat(result.content).isEmpty()
    }

    @Test
    fun `getAllOnSalePaged - 유효하지 않은 페이징 요청 값인 경우 InvalidPageRequestArgumentException 발생`() {
        val page = 0
        val pageSize = 0
        coEvery { service.getAllByStatusOnPaged(any(), any(), any()) } throws InvalidPageRequestArgumentException(0, 0, Sort.by(Sort.Direction.DESC, "createdAt"))

        shouldThrow<InvalidPageRequestArgumentException> {
            facade.getAllOnSalePaged(page, pageSize)
        }
    }
}
