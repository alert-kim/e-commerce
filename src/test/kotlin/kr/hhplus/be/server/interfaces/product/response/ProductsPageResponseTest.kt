package kr.hhplus.be.server.interfaces.product.response

import kr.hhplus.be.server.domain.product.ProductView
import kr.hhplus.be.server.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

class ProductsPageResponseTest {
    @Test
    fun `상품 목록에 대한 응답 생성`() {
        val products = List(3) { ProductMock.view() }
        val pageNumber = 0
        val pageSize = 10
        val pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        val productsPage = PageImpl(products, pageable, products.size.toLong())

        val response = ProductsPageResponse.from(productsPage)

        assertThat(response.totalCount).isEqualTo(products.size.toLong())
        assertThat(response.page).isEqualTo(pageNumber)
        assertThat(response.pageSize).isEqualTo(pageSize)
        assertThat(response.products).hasSize(products.size)
        response.products.forEachIndexed { index, productResponse ->
            assertThat(productResponse.id).isEqualTo(products[index].id.value)
            assertThat(productResponse.name).isEqualTo(products[index].name)
            assertThat(productResponse.description).isEqualTo(products[index].description)
            assertThat(productResponse.price).isEqualByComparingTo(products[index].price)
            assertThat(productResponse.stock).isEqualTo(products[index].stock)
            assertThat(productResponse.createdAt).isEqualTo(products[index].createdAt)
        }

    }

    @Test
    fun `빈 리스트인 경우 빈 리스트 응답`() {
        val products = emptyList<ProductView>()
        val pageNumber = 0
        val pageSize = 10
        val pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        val productsPage = PageImpl(products, pageable, 0)

        val response = ProductsPageResponse.from(productsPage)

        assertThat(response.totalCount).isEqualTo(0)
        assertThat(response.page).isEqualTo(pageNumber)
        assertThat(response.pageSize).isEqualTo(pageSize)
        assertThat(response.products).isEmpty()
    }
}

