package kr.hhplus.be.server.interfaces.product

import kr.hhplus.be.server.domain.product.ProductStatus
import kr.hhplus.be.server.interfaces.ApiTest
import kr.hhplus.be.server.interfaces.ErrorCode
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.springframework.test.web.servlet.get

@Isolated
class GetProductsApiTest : ApiTest() {

    @BeforeEach
    fun setup() {
        clearProducts()
    }

    @Test
    @DisplayName("지정된 상태의 상품 목록이 조회된다")
    fun getProductsByStatus() {
        val totalProducts = 5
        val page = 0
        val pageSize = 10
        val products = List(totalProducts) { index ->
            savedProduct(status = ProductStatus.ON_SALE)
        }
        savedProduct(status = ProductStatus.INACTIVE)

        mockMvc.get("/products") {
            param("page", page.toString())
            param("pageSize", pageSize.toString())
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalCount") { value(totalProducts) }
            jsonPath("$.page") { value(0) }
            jsonPath("$.pageSize") { value(10) }
            jsonPath("$.products", hasSize<Any>(totalProducts))
            products.forEachIndexed { index, _ ->
                jsonPath("$.products[$index].status") { value(ProductStatus.ON_SALE.name) }
            }
            jsonPath("$.products[0].id") { isNumber() }
            jsonPath("$.products[0].status") { isNotEmpty() }
            jsonPath("$.products[0].name") { isString() }
            jsonPath("$.products[0].description") { isString() }
            jsonPath("$.products[0].price") { isNumber() }
            jsonPath("$.products[0].stock") { isNumber() }
            jsonPath("$.products[0].createdAt") { isString() }
        }
    }

    @Test
    @DisplayName("페이징이 정상적으로 동작한다")
    fun pagination() {
        val totalProducts = 10
        val pageSize = 5
        List(totalProducts) {
            savedProduct(status = ProductStatus.ON_SALE)
        }

        // 첫 번째 페이지
        mockMvc.get("/products") {
            param("page", "0")
            param("pageSize", "$pageSize")
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalCount") { value(totalProducts) }
            jsonPath("$.page") { value(0) }
            jsonPath("$.pageSize") { value(pageSize) }
            jsonPath("$.products") { hasSize<Any>(pageSize) }
        }

        // 두 번째 페이지
        mockMvc.get("/products") {
            param("page", "1")
            param("pageSize", "$pageSize")
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalCount") { value(totalProducts) }
            jsonPath("$.page") { value(1) }
            jsonPath("$.pageSize") { value(pageSize) }
            jsonPath("$.products") { hasSize<Any>(pageSize) }
        }
    }

    @Test
    @DisplayName("상품이 없으면 빈 목록이 반환된다")
    fun emptyProductList() {
        mockMvc.get("/products") {
            param("page", "0")
            param("pageSize", "10")
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalCount") { value(0) }
            jsonPath("$.page") { value(0) }
            jsonPath("$.pageSize") { value(10) }
            jsonPath("$.products") { hasSize<Any>(0) }
        }
    }

    @Test
    @DisplayName("잘못된 페이지 파라미터로 요청하면 400 오류가 발생한다")
    fun invalidPageParameters() {
        mockMvc.get("/products") {
            param("page", "-1")
            param("pageSize", "0")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value(ErrorCode.INVALID_REQUEST.name) }
        }
    }
}
