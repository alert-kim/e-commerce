package kr.hhplus.be.server.interfaces.coupon.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.interfaces.CouponErrorCode.ALREADY_USED_COUPON_CODE
import kr.hhplus.be.server.interfaces.CouponErrorCode.NOT_FOUND_COUPON_CODE
import kr.hhplus.be.server.interfaces.CouponErrorCode.OUT_OF_STOCK_COUPON_CODE
import kr.hhplus.be.server.interfaces.ErrorResponse
import kr.hhplus.be.server.interfaces.UserApiErrorCode.NOT_FOUND_USER_CODE
import kr.hhplus.be.server.interfaces.common.api.ServerApiResponse
import kr.hhplus.be.server.interfaces.coupon.api.request.IssueCouponRequest
import kr.hhplus.be.server.interfaces.coupon.api.response.CouponResponse
import kr.hhplus.be.server.interfaces.coupon.api.response.CouponSourcesResponse
import kr.hhplus.be.server.interfaces.coupon.api.response.CouponsResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody


@Tag(name = "Coupon API", description = "쿠폰 관련 API")
interface CouponControllerInterface {

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
                            implementation = CouponSourcesResponse::class,
                        )
                    )
                ]
            ),
        ]
    )
    @GetMapping("/couponSources")
    fun getCouponSources(
    ): ResponseEntity<ServerApiResponse>

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
                            implementation = CouponResponse::class,
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
                                name = "찾을 수 없는 쿠폰 소스",
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
    @PostMapping("/coupons")
    fun issueCoupon(
        @RequestBody request: IssueCouponRequest,
    ): ResponseEntity<ServerApiResponse>

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
                            implementation = CouponsResponse::class,
                        )
                    )
                ]
            ),
            ApiResponse(
                responseCode = "404",
                description = "리소스를 찾을 수 없음",
                content = [Content(
                    examples = [ExampleObject(value = """{"code":"NOT_FOUND_USER"}""")],
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            )
        ]
    )
    @GetMapping("/user/{userId}/coupons")
    fun getUserCoupons(
        @PathVariable userId: Long,
    ): ResponseEntity<ServerApiResponse>
}
