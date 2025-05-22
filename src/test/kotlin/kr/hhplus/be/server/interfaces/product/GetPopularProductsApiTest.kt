package kr.hhplus.be.server.interfaces.product

import io.kotest.property.Arb
import io.kotest.property.arbitrary.localDate
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.common.cache.CacheNames
import kr.hhplus.be.server.common.util.TimeZone
import kr.hhplus.be.server.interfaces.ApiTest
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.test.web.servlet.get
import java.time.LocalDate

@Isolated
class GetPopularProductsApiTest @Autowired constructor(
    private val cacheManager: CacheManager
) : ApiTest() {

    @BeforeEach
    fun setup() {
        deleteAllProductSaleRanking()
        cacheManager.getCache(CacheNames.POPULAR_PRODUCTS)?.clear()
    }

    @Test
    @DisplayName("최대 5개의 인기 상품만 조회된다")
    fun maxFivePopularProducts() {
        val productsSize = 20
        val popularProductsSize = 5
        val products = List(productsSize) { index ->
            val product = savedProduct()
            updateProductSaleRanking(
                productId = product.id(),
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
    @DisplayName("최근 1-3일 사이의 인기 상품만 조회된다")
    fun recentPopularProducts() {
        val startDate = LocalDate.now(TimeZone.KSTId).minusDays(2)
        val endDate = LocalDate.now(TimeZone.KSTId)
        val popularProducts = List(5) { index ->
            val product = savedProduct()
            val date = Arb.localDate(minDate = startDate, maxDate = endDate).next()
            updateProductSaleRanking(
                productId = product.id(),
                date = date,
                quantity = 100 - index * 10,
            )
            product
        }
        updateProductSaleRanking(
            productId = savedProduct().id(),
            date = startDate.minusDays(1),
            quantity = 1000,
        )
        updateProductSaleRanking(
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
    @DisplayName("인기 상품이 없으면 빈 목록이 반환된다")
    fun noPopularProducts() {
        savedProduct()

        mockMvc.get("/products/popular").andExpect {
            status { isOk() }
            jsonPath("$.products") { isArray() }
            jsonPath("$.products", hasSize<Any>(0))
        }
    }
}
