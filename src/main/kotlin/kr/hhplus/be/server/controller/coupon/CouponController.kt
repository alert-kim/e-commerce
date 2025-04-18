package kr.hhplus.be.server.controller.coupon

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.controller.BalanceApiErrorCode.EXCEED_MAX_BALANCE_CODE
import kr.hhplus.be.server.controller.BalanceApiErrorCode.NOT_FOUND_BALANCE_CODE
import kr.hhplus.be.server.controller.CouponErrorCode.ALREADY_USED_COUPON_CODE
import kr.hhplus.be.server.controller.CouponErrorCode.INVALID_STATE_COUPON_CODE
import kr.hhplus.be.server.controller.CouponErrorCode.NOT_FOUND_COUPON_CODE
import kr.hhplus.be.server.controller.CouponErrorCode.OUT_OF_STOCK_COUPON_CODE
import kr.hhplus.be.server.controller.ErrorResponse
import kr.hhplus.be.server.controller.UserApiErrorCode.NOT_FOUND_USER_CODE
import kr.hhplus.be.server.controller.balance.request.ChargeApiRequest
import kr.hhplus.be.server.controller.balance.response.UserBalanceResponse
import kr.hhplus.be.server.controller.coupon.request.IssueCouponRequest
import kr.hhplus.be.server.controller.coupon.response.CouponsResponse
import kr.hhplus.be.server.controller.coupon.response.CouponsResponse.CouponResponse
import kr.hhplus.be.server.controller.coupon.response.UserCouponResponse
import kr.hhplus.be.server.controller.coupon.response.UserCouponsResponse
import kr.hhplus.be.server.model.coupon.CouponType
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.Instant


@RestController
@Tag(name = "Coupon API", description = "쿠폰 관련 API")
class CouponController {

    @Operation(
        summary = "쿠폰 조회",
        description = "발급 가능한 쿠폰 목록이 조회된다 (발급 전)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "쿠폰 조회 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(
                            implementation = CouponsResponse::class,
                        )
                    )
                ]
            ),
        ]
    )
    @GetMapping("/coupons")
    fun getCoupons(
    ): CouponsResponse =
        CouponsResponse(
            coupons = listOf(
                CouponResponse(
                    id = 1L,
                    name = "쿠폰1",
                    type = CouponType.FIXED_AMOUNT,
                    quantity = 10,
                    maxDiscountAmount = BigDecimal.valueOf(10_000),
                    discountRate = null,
                    discountAmount = BigDecimal.valueOf(1_000),
                    usableFrom = Instant.parse("2023-01-01T00:00:00Z"),
                    usableTo = Instant.parse("2025-12-31T23:59:59Z"),
                ),
            )
        )

    @Operation(
        summary = "유저 쿠폰 조회",
        description = "유저의 쿠폰 목록이 조회된다 (발급 후)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "쿠폰 조회 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(
                            implementation = UserCouponsResponse::class,
                        )
                    )
                ]
            ),
        ]
    )
    @GetMapping("/user/{userId}/coupons")
    fun getUserCoupons(
        @PathVariable userId: Long,
    ): UserCouponsResponse =
        UserCouponsResponse(
            coupons = listOf(
                UserCouponResponse(
                    id = 1L,
                    userId = userId,
                    coupon = CouponResponse(
                        id = 1L,
                        name = "쿠폰1",
                        type = CouponType.FIXED_AMOUNT,
                        quantity = 10,
                        maxDiscountAmount = BigDecimal.valueOf(10_000),
                        discountRate = null,
                        discountAmount = BigDecimal.valueOf(1_000),
                        usableFrom = Instant.parse("2023-01-01T00:00:00Z"),
                        usableTo = Instant.parse("2025-12-31T23:59:59Z"),
                    ),
                )
            )
        )

    @Operation(
        summary = "쿠폰 발급",
        description = "쿠폰을 발급함"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "쿠폰 발급 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(
                            implementation = UserCouponResponse::class,
                            description = "발급된 유저 쿠폰"
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
                                name = "쿠폰 재고 소진",
                                value = OUT_OF_STOCK_COUPON_CODE,
                                summary = "OUT_OF_STOCK_COUPON"
                            ),
                            ExampleObject(
                                name = "발급할 수 없는 쿠폰",
                                value = INVALID_STATE_COUPON_CODE,
                                summary = "INVALID_STATE_COUPON"
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
                                name = "찾을 수 없는 쿠폰",
                                value = NOT_FOUND_COUPON_CODE,
                                summary = "NOT_FOUND_COUPON"
                            ),
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
            ApiResponse(
                responseCode = "409",
                description = "요청이 현재 상태와 충돌",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                name = "이미 발급된 쿠폰",
                                value = ALREADY_USED_COUPON_CODE,
                                summary = "ALREADY_HAS_COUPON"
                            ),
                        ],
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponse::class)
                    ),
                ]
            ),
        ]
    )
    @PostMapping("/coupons:issue")
    fun issueCoupon(
        @RequestBody request: IssueCouponRequest,
    ): UserCouponResponse =
        UserCouponResponse(
            id = 1L,
            userId = 2L,
            coupon = CouponResponse(
                id = 1L,
                name = "쿠폰1",
                type = CouponType.FIXED_AMOUNT,
                quantity = 10,
                maxDiscountAmount = BigDecimal.valueOf(10_000),
                discountRate = null,
                discountAmount = BigDecimal.valueOf(1_000),
                usableFrom = Instant.parse("2023-01-01T00:00:00Z"),
                usableTo = Instant.parse("2025-12-31T23:59:59Z"),
            ),
        )
}
