package kr.hhplus.be.server.application.product.result

import io.kotest.assertions.throwables.shouldThrow
import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.testutil.mock.ProductMock
import kr.hhplus.be.server.testutil.mock.StockMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class ProductsResultTest {

    @Test
    fun `Paged 클래스의 from - ProductView에 대한 페이지와 StockView의 List로 Paged ProductWithStock을 생성` () {
        val products = List(2) {
            ProductMock.view()
        }
        val stocks = products.map { StockMock.view(productId = it.id) }
        val productPage = PageImpl(
            products,
            PageRequest.of(0, 10),
            products.size.toLong()
        )

        val result = ProductsResult.Paged.from(productPage, stocks)

        assertThat(result.value.totalElements).isEqualTo(productPage.totalElements)
        assertThat(result.value.number).isEqualTo(productPage.number)
        assertThat(result.value.size).isEqualTo(productPage.size)
        result.value.content.forEachIndexed { index, productWithStock ->
            val product = products[index]
            val stock = stocks[index]

            assertThat(productWithStock.product.id.value).isEqualTo(product.id.value)
            assertThat(productWithStock.stockQuantity).isEqualTo(stock.quantity)
        }
    }

    @Test
    fun `Paged 클래스의 from - 재고가 없으면 에러` () {
        val products = listOf(ProductMock.view(id = ProductId(1L)))
        val stocks = listOf(StockMock.view(productId = ProductId(2L)))
        val productPage = PageImpl(
            products,
            PageRequest.of(0, 10),
            products.size.toLong()
        )

        shouldThrow<NoSuchElementException> {
            ProductsResult.Paged.from(productPage, stocks)
        }
    }


    @Test
    fun `Listed 클래스의 from - ProductView에 대한 List와 StockView의 List로 Listed ProductWithStock을 생성` () {
        val products = List(2) {
            ProductMock.view()
        }
        val stocks = products.map { StockMock.view(productId = it.id) }

        val result = ProductsResult.Listed.from(products, stocks)

        assertThat(result.value).hasSize(products.size)
        result.value.forEachIndexed { index, productWithStock ->
            val product = products[index]
            val stock = stocks[index]

            assertThat(productWithStock.product.id.value).isEqualTo(product.id.value)
            assertThat(productWithStock.stockQuantity).isEqualTo(stock.quantity)
        }
    }

    @Test
    fun `Listed 클래스의 from - 재고가 없으면 에러` () {
        val products = listOf(ProductMock.view(id = ProductId(1L)))
        val stocks = listOf(StockMock.view(productId = ProductId(2L)))

        shouldThrow<NoSuchElementException> {
            ProductsResult.Listed.from(products, stocks)
        }
    }
}
