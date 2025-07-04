package kr.hhplus.be.server.interfaces.order.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.interfaces.BalanceApiErrorCode.INSUFFICIENT_BALANCE_CODE
import kr.hhplus.be.server.interfaces.CouponErrorCode.ALREADY_USED_COUPON_CODE
import kr.hhplus.be.server.interfaces.CouponErrorCode.EXPIRED_COUPON_CODE
import kr.hhplus.be.server.interfaces.CouponErrorCode.NOT_FOUND_COUPON_CODE
import kr.hhplus.be.server.interfaces.CouponErrorCode.NOT_OWNED_COUPON_CODE
import kr.hhplus.be.server.interfaces.ErrorResponse
import kr.hhplus.be.server.interfaces.OrderErrorCode.INVALID_ORDER_PRICE_CODE
import kr.hhplus.be.server.interfaces.ProductErrorCode.NOT_FOUND_PRODUCT_CODE
import kr.hhplus.be.server.interfaces.ProductErrorCode.OUT_OF_PRODUCT_STOCK_CODE
import kr.hhplus.be.server.interfaces.UserApiErrorCode.NOT_FOUND_USER_CODE
import kr.hhplus.be.server.interfaces.common.api.ServerApiResponse
import kr.hhplus.be.server.interfaces.order.api.reqeust.OrderRequest
import kr.hhplus.be.server.interfaces.order.api.response.OrderResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/orders")
@Tag(name = "Order API", description = "주문 관련 API")
interface OrderControllerInterface {

    @Operation(
        summary = "주문",
        description = "상품을 주문함"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "주문 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(
                            implementation = OrderResponse::class,
                            description = "주문 성공"
                        )
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                name = "상품 재고 부족",
                                value = OUT_OF_PRODUCT_STOCK_CODE,
                                summary = "OUT_OF_PRODUCT_STOCK"
                            ),
                            ExampleObject(
                                name = "잘못된 주문 가격",
                                value = INVALID_ORDER_PRICE_CODE,
                                summary = "INVALID_ORDER_PRICE"
                            ),
                            ExampleObject(
                                name = "이미 사용된 쿠폰",
                                value = ALREADY_USED_COUPON_CODE,
                                summary = "ALREADY_USED_COUPON"
                            ),
                            ExampleObject(
                                name = "쿠폰 사용 기간 만료",
                                value = EXPIRED_COUPON_CODE,
                                summary = "EXPIRED_COUPON"
                            ),
                            ExampleObject(
                                name = "잔고 부족",
                                value = INSUFFICIENT_BALANCE_CODE,
                                summary = "INSUFFICIENT_BALANCE"
                            ),
                        ],
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponse::class)
                    ),
                ]
            ),
            ApiResponse(
                responseCode = "403",
                description = "권한 없음",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                name = "쿠폰을 소유하고 있지 않음",
                                value = NOT_OWNED_COUPON_CODE,
                                summary = "NOT_OWNED_COUPON"
                            ),
                        ],
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponse::class)
                    ),
                ]
            ),
            ApiResponse(
                responseCode = "404",
                description = "리소스를 찾을 수 없음",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                name = "찾을 수 없는 유저",
                                value = NOT_FOUND_USER_CODE,
                                summary = "NOT_FOUND_USER"
                            ),
                            ExampleObject(
                                name = "찾을 수 없는 상품",
                                value = NOT_FOUND_PRODUCT_CODE,
                                summary = "NOT_FOUND_PRODUCT"
                            ),
                            ExampleObject(
                                name = "찾을 수 없는 쿠폰",
                                value = NOT_FOUND_COUPON_CODE,
                                summary = "NOT_FOUND_COUPON"
                            ),
                        ],
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponse::class)
                    ),
                ]
            ),
        ]
    )
    @PostMapping("")
    fun order(
        @RequestBody request: OrderRequest,
    ): ResponseEntity<ServerApiResponse>
}

