package kr.hhplus.be.server.interfaces.product.response

import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.application.product.result.ProductsResult
import kr.hhplus.be.server.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class ProductsPageResponseTest {
    @Test
    fun `ProductsResult Paged에서 ProductsPageResponse로 변환한다`() {
        val productsWithStock = List(3) {
            ProductsResult.ProductWithStock(
                product = ProductMock.view(),
                stockQuantity = Arb.int(2..5).next()
            )
        }
        val pagedResult = ProductsResult.Paged(
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
