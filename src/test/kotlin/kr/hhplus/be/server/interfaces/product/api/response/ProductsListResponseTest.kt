package kr.hhplus.be.server.interfaces.product.api.response

import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.application.product.result.GetProductsFacadeResult
import kr.hhplus.be.server.interfaces.product.api.response.ProductsListResponse
import kr.hhplus.be.server.testutil.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProductsListResponseTest {

    @Nested
    @DisplayName("응답 변환")
    inner class Convert {
        @Test
        @DisplayName("목록 형태의 상품 정보를 응답으로 변환한다")
        fun fromListed() {
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
}
