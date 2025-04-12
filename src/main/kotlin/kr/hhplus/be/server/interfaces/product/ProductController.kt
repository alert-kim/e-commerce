package kr.hhplus.be.server.interfaces.product

import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.product.ProductStatus
import kr.hhplus.be.server.interfaces.ErrorCode
import kr.hhplus.be.server.interfaces.ErrorSpec
import kr.hhplus.be.server.interfaces.common.ServerApiResponse
import kr.hhplus.be.server.interfaces.common.handleRequest
import kr.hhplus.be.server.interfaces.product.response.ProductResponse
import kr.hhplus.be.server.interfaces.product.response.ProductsResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.function.ServerResponse
import java.math.BigDecimal
import java.time.Instant

@RestController
class ProductController(
    private val productService: ProductService,
) : ProductControllerInterface {

    @GetMapping("/products")
    override fun getProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") pageSize: Int,
    ) = handleRequest(
        block = {
            val productsPaged = productService.getAllOnSalePaged(page = page, size = pageSize)

            ProductsResponse(
                totalCount = productsPaged.totalElements,
                page = productsPaged.number,
                pageSize = productsPaged.size,
                products = productsPaged.content.map { product ->
                    ProductResponse.from(product)
                }
            )
        },
        errorSpec = {
            when (it) {
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
