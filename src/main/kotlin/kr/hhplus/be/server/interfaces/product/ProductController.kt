package kr.hhplus.be.server.interfaces.product

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.interfaces.product.response.ProductResponse
import kr.hhplus.be.server.interfaces.product.response.ProductsResponse
import kr.hhplus.be.server.domain.product.ProductStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class ProductController : ProductControllerInterface {

    @GetMapping("/products")
    override fun getProducts(): ProductsResponse = ProductsResponse(
        products = listOf(
            ProductResponse(
                id = 1L,
                status = ProductStatus.ON_SALE,
                name = "상품1",
                description = "상품1 설명",
                price = 10000,
                stock = 10,
                createdAt = Instant.now(),
            ),
            ProductResponse(
                id = 2L,
                status = ProductStatus.ON_SALE,
                name = "상품2",
                description = "상품2 설명",
                price = 20000,
                stock = 20,
                createdAt = Instant.now(),
            )
        )
    )

    @GetMapping("/products:popular")
    override fun getPopularProducts(): ProductsResponse = ProductsResponse(
        products = listOf(
            ProductResponse(
                id = 1L,
                status = ProductStatus.ON_SALE,
                name = "상품1",
                description = "상품1 설명",
                price = 10000,
                stock = 10,
                createdAt = Instant.now(),
            ),
            ProductResponse(
                id = 2L,
                status = ProductStatus.ON_SALE,
                name = "상품2",
                description = "상품2 설명",
                price = 20000,
                stock = 20,
                createdAt = Instant.now(),
            )
        )
    )

}
