package kr.hhplus.be.server.interfaces.product

import kr.hhplus.be.server.domain.product.ProductStatus
import kr.hhplus.be.server.interfaces.ApiTest
import kr.hhplus.be.server.interfaces.ErrorCode
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
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
    fun `상품 목록 조회 - 200 - 해당 상태의 상품 조회`() {
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
    fun `상품 목록 조회 - 200 - 페이징 정상 동작`() {
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
    fun `상품 목록 조회 - 200 - 상품 없음`() {
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
    fun `상품 목록 조회 - 400 - 잘못된 page, pageSize`() {
        mockMvc.get("/products") {
            param("page", "-1")
            param("pageSize", "0")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value(ErrorCode.INVALID_REQUEST.name) }
        }
    }
}
