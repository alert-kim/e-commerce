package kr.hhplus.be.server.interfaces.order

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.interfaces.ErrorResponse
import kr.hhplus.be.server.interfaces.OrderErrorCode.INVALID_ORDER_PRICE_CODE
import kr.hhplus.be.server.interfaces.OrderErrorCode.OUT_OF_PRODUCT_STOCK_CODE
import kr.hhplus.be.server.interfaces.UserApiErrorCode.NOT_FOUND_USER_CODE
import kr.hhplus.be.server.interfaces.order.reqeust.OrderRequest
import kr.hhplus.be.server.interfaces.order.response.OrderResponse
import kr.hhplus.be.server.domain.order.OrderStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/orders")
@Tag(name = "Order API", description = "주문 관련 API")
class OrderController {

    @Operation(
        summary = "주문",
        description = "주문을 시작함"
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
                                name = "잘못된 주문 가격",
                                value = INVALID_ORDER_PRICE_CODE,
                                summary = "INVALID_ORDER_PRICE"
                            ),
                            ExampleObject(
                                name = "상품 재고 부족",
                                value = OUT_OF_PRODUCT_STOCK_CODE,
                                summary = "OUT_OF_PRODUCT_STOCK"
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
                        ],
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponse::class)
                    ),
                ]
            ),
        ]
    )
    @PostMapping("")
    fun charge(
        @RequestBody request: OrderRequest,
    ): OrderResponse =
        OrderResponse(
            id = 1L,
            userId = request.userId,
            totalPrice = request.totalPrice,
            status = OrderStatus.READY,
            expiresAt = Instant.now().plusSeconds(60),
            orderItems = request.orderItems.map {
                OrderResponse.OrderItem(
                    productId = it.productId,
                    quantity = it.quantity,
                    price = it.price,
                )
            },
            createdAt = Instant.now(),
        )
}
