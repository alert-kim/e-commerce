package kr.hhplus.be.server.controller.payment

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.controller.CouponErrorCode.ALREADY_USED_COUPON_CODE
import kr.hhplus.be.server.controller.CouponErrorCode.EXPIRED_COUPON_CODE
import kr.hhplus.be.server.controller.CouponErrorCode.NOT_FOUND_COUPON_CODE
import kr.hhplus.be.server.controller.ErrorResponse
import kr.hhplus.be.server.controller.OrderErrorCode.ALREADY_PAID_ORDER_CODE
import kr.hhplus.be.server.controller.OrderErrorCode.EXPIRED_ORDER_CODE
import kr.hhplus.be.server.controller.OrderErrorCode.INVALID_ORDER_PRICE_CODE
import kr.hhplus.be.server.controller.OrderErrorCode.NOT_FOUND_ORDER_CODE
import kr.hhplus.be.server.controller.OrderErrorCode.OUT_OF_PRODUCT_STOCK_CODE
import kr.hhplus.be.server.controller.UserApiErrorCode.NOT_FOUND_USER_CODE
import kr.hhplus.be.server.controller.order.reqeust.OrderRequest
import kr.hhplus.be.server.controller.order.response.OrderResponse
import kr.hhplus.be.server.controller.payment.requeset.PayRequest
import kr.hhplus.be.server.controller.payment.respnse.PaymentResponse
import kr.hhplus.be.server.model.order.OrderStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/payments")
@Tag(name = "Payment API", description = "결제 관련 API")
class PaymentController {
    @Operation(
        summary = "결제",
        description = "결제 진행"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "결제 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(
                            implementation = PaymentResponse::class,
                            description = "결제 성공"
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
                                name = "잘못된 가격(결제 금액오류)",
                                value = INVALID_ORDER_PRICE_CODE,
                                summary = "INVALID_ORDER_PRICE"
                            ),
                            ExampleObject(
                                name = "이미 결제된 주문",
                                value = ALREADY_PAID_ORDER_CODE,
                                summary = "ALREADY_PAID_ORDER"
                            ),
                            ExampleObject(
                                name = "만료된 주문",
                                value = EXPIRED_ORDER_CODE,
                                summary = "EXPIRED_ORDER_CODE"
                            ),
                            ExampleObject(
                                name = "상품 재고 부족",
                                value = OUT_OF_PRODUCT_STOCK_CODE,
                                summary = "OUT_OF_PRODUCT_STOCK"
                            ),
                            ExampleObject(
                                name = "이미 사용된 쿠폰",
                                value = ALREADY_USED_COUPON_CODE,
                                summary = "ALREADY_USED_COUPON"
                            ),
                            ExampleObject(
                                name = "만료된 쿠폰",
                                value = EXPIRED_COUPON_CODE,
                                summary = "EXPIRED_COUPON_CODE"
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
                                name = "찾을 수 없는 주문",
                                value = NOT_FOUND_ORDER_CODE,
                                summary = "NOT_FOUND_ORDER"
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
    fun pay(
        @RequestBody request: PayRequest,
    ): PaymentResponse =
        PaymentResponse(
            id = 1L,
            orderId = request.orderId,
            userId = request.userId,
            couponId = request.couponId,
            originalAmount = request.originalAmount,
            discountAmount = request.discountAmount,
            finalAmount = request.finalAmount,
        )
}
