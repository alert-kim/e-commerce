package kr.hhplus.be.server.interfaces.product.response

import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.application.product.result.GetProductsFacadeResult
import kr.hhplus.be.server.testutil.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ProductsListResponseTest {
    @Test
    fun `ProductsResult의 Listed에서 ProductsListResponse로 변환한다`() {
        val productsWithStock = List(3) {
            GetProductsFacadeResult.ProductWithStock(
                product = ProductMock.view(),
                stockQuantity = Arb.int(2..5).next()
            )
        }

        val response = ProductsListResponse.from(GetProductsFacadeResult.Listed(productsWithStock))

        assertThat(response.products).hasSize(productsWithStock.size)
        productsWithStock.forEachIndexed { index, productWithStock ->
            val productResponse = response.products[index]
            assertThat(productResponse.id).isEqualTo(productWithStock.product.id.value)
            assertThat(productResponse.stock).isEqualTo(productWithStock.stockQuantity)
        }
    }
}
