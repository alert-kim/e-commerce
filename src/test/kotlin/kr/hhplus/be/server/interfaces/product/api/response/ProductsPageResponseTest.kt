package kr.hhplus.be.server.interfaces.product.api.response

import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.application.product.result.GetProductsFacadeResult
import kr.hhplus.be.server.interfaces.product.api.response.ProductsPageResponse
import kr.hhplus.be.server.testutil.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class ProductsPageResponseTest {

    @Nested
    @DisplayName("응답 변환")
    inner class Convert {
        @Test
        @DisplayName("페이지 형태의 상품 정보를 응답으로 변환한다")
        fun fromPaged() {
            val productsWithStock = List(3) {
                GetProductsFacadeResult.ProductWithStock(
                    product = ProductMock.view(),
                    stockQuantity = Arb.int(2..5).next()
                )
            }
            val pagedResult = GetProductsFacadeResult.Paged(
                value = PageImpl(productsWithStock, PageRequest.of(0, 10), 1)
            )

            val response = ProductsPageResponse.from(pagedResult)

            assertThat(response.totalCount).isEqualTo(pagedResult.value.totalElements)
            assertThat(response.page).isEqualTo(pagedResult.value.number)
            assertThat(response.pageSize).isEqualTo(pagedResult.value.size)
            assertThat(response.products).hasSize(pagedResult.value.content.size)
            pagedResult.value.content.forEachIndexed { index, productWithStock ->
                val productResponse = response.products[index]
                assertThat(productResponse.id).isEqualTo(productWithStock.product.id.value)
                assertThat(productResponse.stock).isEqualTo(productWithStock.stockQuantity)
            }
        }
    }
}
