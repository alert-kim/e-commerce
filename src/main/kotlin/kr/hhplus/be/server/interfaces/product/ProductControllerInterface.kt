package kr.hhplus.be.server.interfaces.product

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.interfaces.common.ServerApiResponse
import kr.hhplus.be.server.interfaces.product.response.ProductsResponse
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "Product API", description = "상품 관련 API")
interface ProductControllerInterface {

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
    fun getProducts(
        @RequestParam page: Int,
        @RequestParam(defaultValue = "20") pageSize: Int,
    ): ResponseEntity<ServerApiResponse>

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
    fun getPopularProducts(): ProductsResponse
}
