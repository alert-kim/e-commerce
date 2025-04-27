package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ProductsViewTest {
    @Test
    fun `from - Product 리스트를 ProductsView로 변환`() {
        val products = List(3) { ProductMock.product() }

        val result = ProductsView.from(products)

        assertThat(result.value).hasSize(products.size)
        result.value.forEachIndexed { index, productView ->
            assertThat(productView.id).isEqualTo(products[index].id())
        }
    }

    @Test
    fun `from - 빈 Product 리스트를 ProductsView로 변환`() {
        val products = emptyList<Product>()

        val result = ProductsView.from(products)

        assertThat(result.value).isEmpty()
    }
}
