package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.testutil.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProductsViewTest {

    @Nested
    @DisplayName("from 메서드")
    inner class From {

        @Test
        @DisplayName("Product 리스트를 ProductsView로 변환")
        fun convertList() {
            val products = List(3) { ProductMock.product() }

            val result = ProductsView.from(products)

            assertThat(result.value).hasSize(products.size)
            result.value.forEachIndexed { index, productView ->
                assertThat(productView.id).isEqualTo(products[index].id())
            }
        }

        @Test
        @DisplayName("Product 리스트가 비어있을 때 ProductsView로 변환")
        fun convertEmptyList() {
            val products = emptyList<Product>()

            val result = ProductsView.from(products)

            assertThat(result.value).isEmpty()
        }
    }
}
