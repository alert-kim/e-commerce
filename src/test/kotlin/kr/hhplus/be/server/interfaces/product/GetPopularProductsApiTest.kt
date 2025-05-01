package kr.hhplus.be.server.interfaces.product

import io.kotest.property.Arb
import io.kotest.property.arbitrary.localDate
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.interfaces.ApiTest
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import java.time.LocalDate

class GetPopularProductsApiTest : ApiTest() {

    @Test
    fun `인기 상품 조회 - 200 - 최대 5개만 조회`() {
        val productsSize = 20
        val popularProductsSize = 5
        val products = List(productsSize) { index ->
            val product = savedProduct()
            savedProductDailySale(
                productId = product.id(),
                date = LocalDate.now(),
                quantity = 100 - index * 10,
            )
            product
        }

        mockMvc.get("/products/popular").andExpect {
            status { isOk() }
            jsonPath("$.products") { isArray() }
            jsonPath("$.products", hasSize<Any>(popularProductsSize))
            repeat(popularProductsSize) { index ->
                jsonPath("$.products[$index].id") { value(products[index].id().value) }
                jsonPath("$.products[$index].status") { value(products[index].status.name) }
                jsonPath("$.products[$index].name") { value(products[index].name) }
                jsonPath("$.products[$index].description") { value(products[index].description) }
                jsonPath("$.products[$index].price") { value(products[index].price.toDouble()) }
                jsonPath("$.products[$index].stock") { isNumber() }
            }
        }
    }

    @Test
    fun `인기 상품 조회 - 200 - 오늘 부터 2일 전까지에 대한 인기 상품 조회`() {
        val startDate = LocalDate.now().minusDays(2)
        val endDate = LocalDate.now()
        val popularProducts = List(5) { index ->
            val product = savedProduct()
            val date = Arb.localDate(minDate = startDate, maxDate = endDate).next()
            savedProductDailySale(
                productId = product.id(),
                date = date,
                quantity = 100 - index * 10,
            )
            product
        }
        savedProductDailySale(
            productId = savedProduct().id(),
            date = startDate.minusDays(1),
            quantity = 1000,
        )
        savedProductDailySale(
            productId = savedProduct().id(),
            date = endDate.plusDays(1),
            quantity = 1000,
        )

        mockMvc.get("/products/popular").andExpect {
            status { isOk() }
            jsonPath("$.products") { isArray() }
            jsonPath("$.products", hasSize<Any>(popularProducts.size))
            repeat(5) { index ->
                jsonPath("$.products[$index].id") { value(popularProducts[index].id().value) }
                jsonPath("$.products[$index].status") { value(popularProducts[index].status.name) }
                jsonPath("$.products[$index].name") { value(popularProducts[index].name) }
                jsonPath("$.products[$index].description") { value(popularProducts[index].description) }
                jsonPath("$.products[$index].price") { value(popularProducts[index].price.toDouble()) }
                jsonPath("$.products[$index].stock") { isNumber() }
            }
        }
    }

    @Test
    fun `인기 상품 조회 - 200 - 인기 상품 없음`() {
        savedProduct()

        mockMvc.get("/products/popular").andExpect {
            status { isOk() }
            jsonPath("$.products") { isArray() }
            jsonPath("$.products", hasSize<Any>(0))
        }
    }
}
