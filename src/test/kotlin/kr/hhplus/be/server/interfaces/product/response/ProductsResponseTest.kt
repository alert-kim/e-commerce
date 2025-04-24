package kr.hhplus.be.server.interfaces.product.response

import kr.hhplus.be.server.application.product.result.ProductsResult
import kr.hhplus.be.server.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl

class ProductsResponseTest {
    @Test
    fun `ProductResult Paged에서 ProductsResponse로 변환한다`() {
        val productView = ProductMock.view()
        val result = ProductsResult.Paged(PageImpl(listOf(productView)))

        val response = ProductsResponse.from(result)

        assertThat(response.products).hasSize(1)
        assertThat(response.products[0].id).isEqualTo(productView.id.value)
        assertThat(response.products[0].name).isEqualTo(productView.name)
    }

    @Test
    fun `ProductResult Listed에서 ProductsResponse로 변환한다`() {
        val productView = ProductMock.view()
        val result = ProductsResult.Listed(listOf(productView))

        val response = ProductsResponse.from(result)

        assertThat(response.products).hasSize(1)
        assertThat(response.products[0].id).isEqualTo(productView.id.value)
        assertThat(response.products[0].name).isEqualTo(productView.name)
    }
}
