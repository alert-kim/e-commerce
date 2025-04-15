package kr.hhplus.be.server.interfaces.product

import kr.hhplus.be.server.domain.common.InvalidPageRequestArgumentException
import kr.hhplus.be.server.application.product.ProductFacade
import kr.hhplus.be.server.domain.product.ProductStatus
import kr.hhplus.be.server.interfaces.ErrorCode
import kr.hhplus.be.server.interfaces.ErrorSpec
import kr.hhplus.be.server.interfaces.common.handleRequest
import kr.hhplus.be.server.interfaces.product.response.ProductResponse
import kr.hhplus.be.server.interfaces.product.response.ProductsResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.Instant

@RestController
class ProductController(
    private val productFacade: ProductFacade,
) : ProductControllerInterface {

    @GetMapping("/products")
    override fun getProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") pageSize: Int,
    ) = handleRequest(
        block = {
            val productsPaged = productFacade.getAllOnSalePaged(page = page, pageSize = pageSize)
            ProductsResponse.from(productsPaged)
        },
        errorSpec = {
            when (it) {
                is InvalidPageRequestArgumentException -> ErrorSpec.badRequest(ErrorCode.INVALID_REQUEST)
                else -> ErrorSpec.serverError(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        }
    )

    @GetMapping("/products:popular")
    override fun getPopularProducts(): ProductsResponse = ProductsResponse(
        totalCount = 0,
        page = 0,
        pageSize = 0,
        products = listOf(
            ProductResponse(
                id = 1L,
                status = ProductStatus.ON_SALE,
                name = "상품1",
                description = "상품1 설명",
                price = BigDecimal.ZERO,
                stock = 10,
                createdAt = Instant.now(),
            ),
            ProductResponse(
                id = 2L,
                status = ProductStatus.ON_SALE,
                name = "상품2",
                description = "상품2 설명",
                price = BigDecimal.ZERO,
                stock = 20,
                createdAt = Instant.now(),
            )
        )
    )
}
