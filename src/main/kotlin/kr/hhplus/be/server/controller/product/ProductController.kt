package kr.hhplus.be.server.controller.product

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.controller.product.response.ProductResponse
import kr.hhplus.be.server.controller.product.response.ProductsResponse
import kr.hhplus.be.server.model.product.ProductStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@Tag(name = "Product API", description = "상품 관련 API")
class ProductController {

    @Operation(
        summary = "상품 조회",
        description = "판매 중인 상품 목록 조회"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "상품 목록 조회 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(
                            implementation = ProductsResponse::class,
                        )
                    )
                ]
            ),
        ]
    )
    @GetMapping("/products")
    fun getProducts(): ProductsResponse = ProductsResponse(
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


    @Operation(
        summary = "인기 상품 조회",
        description = "3일간 판매량이 높은 5개의 상품 조회, 제일 많이 팔린 순으로 반환"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "인기 상품 목록 조회 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(
                            implementation = ProductsResponse::class,
                        )
                    )
                ]
            ),
        ]
    )
    @GetMapping("/products:popular")
    fun getPopularProducts(): ProductsResponse = ProductsResponse(
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
