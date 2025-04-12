package kr.hhplus.be.server.interfaces.product

import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.common.InvalidPageRequestArgumentException
import kr.hhplus.be.server.application.product.ProductFacade
import kr.hhplus.be.server.domain.product.ProductQueryModel
import kr.hhplus.be.server.interfaces.ErrorCode
import kr.hhplus.be.server.mock.ProductMock
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(ProductController::class)
@ExtendWith(MockKExtension::class)
class ProductControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var productFacade: ProductFacade

    @TestConfiguration
    class Config {
        @Bean
        fun productFacade(): ProductFacade = mockk(relaxed = true)
    }

    @BeforeEach
    fun setUp() {
        clearMocks(productFacade)
    }

    @Test
    fun `상품 목록 조회 - 200`() {
        val page = 0
        val pageSize = 20
        val totalCount = 50L
        val products = List(pageSize) {
            ProductMock.queryModel(
                name = "상품${it + 1}",

                )
        }
        every { productFacade.getAllOnSalePaged(page, pageSize) } returns PageImpl(
            products,
            Pageable.unpaged(),
            totalCount
        )

        mockMvc.get("/products?page=${page}&pageSize=${pageSize}")
            .andExpect {
                status { isOk() }
                jsonPath("$.totalCount") { value(totalCount) }
                jsonPath("$.page") { value(page) }
                jsonPath("$.pageSize") { value(pageSize) }
                jsonPath("$.products") { isArray() }
                jsonPath("$.products") { hasSize<Any>(pageSize) }
                products.forEachIndexed { index, productQueryModel ->
                    jsonPath("$.products[$index].name") { value(productQueryModel.name) }
                }
                jsonPath("$.products[0].id") { isNumber() }
                jsonPath("$.products[0].status") { isString() }
                jsonPath("$.products[0].name") { isString() }
                jsonPath("$.products[0].description") { isString() }
                jsonPath("$.products[0].price") { isNumber() }
                jsonPath("$.products[0].stock") { isNumber() }
                jsonPath("$.products[0].createdAt") { isString() }
            }
    }

    @Test
    fun `상품 목록 조회 - 200 - 상품 없음`() {
        val page = 0
        val pageSize = 20
        val totalCount = 0L
        val products = emptyList<ProductQueryModel>()
        every { productFacade.getAllOnSalePaged(page, pageSize) } returns PageImpl(
            products,
            Pageable.unpaged(),
            totalCount
        )

        mockMvc.get("/products?page=${page}&pageSize=${pageSize}")
            .andExpect {
                status { isOk() }
                jsonPath("$.totalCount") { value(totalCount) }
                jsonPath("$.page") { value(page) }
                jsonPath("$.pageSize") { value(0) }
                jsonPath("$.products") { isArray() }
                jsonPath("$.products") { hasSize<Any>(0) }
            }
    }

    @Test
    fun `상품 목록 조회 - 200 - 요청된 page, pageSize로 상품을 조회`() {
        val page = Arb.int(0..10).next()
        val pageSize = Arb.int(1..100).next()
        every { productFacade.getAllOnSalePaged(page, pageSize) } returns PageImpl(emptyList(), Pageable.unpaged(), 0)

        mockMvc.get("/products?page=${page}&pageSize=${pageSize}")
            .andExpect {
                status { isOk() }
            }

        verify { productFacade.getAllOnSalePaged(page, pageSize) }
    }

    @Test
    fun `상품 목록 조회 - 400 - 잘못된 page, pageSize`() {
        val page = -1
        val pageSize = 0
        every { productFacade.getAllOnSalePaged(page, pageSize) } throws InvalidPageRequestArgumentException(
            page = page,
            pageSize = pageSize,
            sort = Sort.by(Sort.Direction.DESC, "createdAt"),
        )

        mockMvc.get("/products?page=${page}&pageSize=${pageSize}")
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.errorCode") { value(ErrorCode.INVALID_REQUEST.name) }
            }

        verify { productFacade.getAllOnSalePaged(page, pageSize) }
    }
}
