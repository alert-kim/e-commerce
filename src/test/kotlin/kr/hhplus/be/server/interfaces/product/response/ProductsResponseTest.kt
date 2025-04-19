package kr.hhplus.be.server.interfaces.product.response

import kr.hhplus.be.server.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ProductsResponseTest {
    @Test
    fun `상품 목록에 대한 응답 생성`() {
        val products = List(3) { ProductMock.queryModel() }

        val response = ProductsResponse.from(products)

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
        val response = ProductsResponse.from(emptyList())

        assertThat(response.products).isEmpty()
    }
}

