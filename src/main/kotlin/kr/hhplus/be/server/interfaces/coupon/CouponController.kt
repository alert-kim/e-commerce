package kr.hhplus.be.server.interfaces.coupon

import kr.hhplus.be.server.application.coupon.CouponFacade
import kr.hhplus.be.server.interfaces.ErrorCode
import kr.hhplus.be.server.interfaces.ErrorSpec
import kr.hhplus.be.server.interfaces.common.ServerApiResponse
import kr.hhplus.be.server.interfaces.common.handleRequest
import kr.hhplus.be.server.interfaces.coupon.request.IssueCouponRequest
import kr.hhplus.be.server.interfaces.coupon.response.CouponSourcesResponse
import kr.hhplus.be.server.interfaces.coupon.response.CouponsResponse.CouponResponse
import kr.hhplus.be.server.interfaces.coupon.response.UserCouponResponse
import kr.hhplus.be.server.interfaces.coupon.response.UserCouponsResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.Instant


@RestController
class CouponController(
    private val couponFacade: CouponFacade,
) : CouponControllerInterface {

    @GetMapping("/coupons")
    override fun getCouponSources(
    ): ResponseEntity<ServerApiResponse>  = handleRequest(
        block = {
            val coupons = couponFacade.getAllIssuable()
            CouponSourcesResponse.from(coupons)
        },
        errorSpec = {
            when (it) {
                else -> ErrorSpec.serverError(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        }
    )
    @GetMapping("/user/{userId}/coupons")
    override fun getUserCoupons(
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

    @PostMapping("/coupons:issue")
    override fun issueCoupon(
        @RequestBody request: IssueCouponRequest,
    ): UserCouponResponse =
        UserCouponResponse(
            id = 1L,
            userId = 2L,
            coupon = CouponResponse(
                id = 1L,
                name = "쿠폰1",
                quantity = 10,
                maxDiscountAmount = BigDecimal.valueOf(10_000),
                discountRate = null,
                discountAmount = BigDecimal.valueOf(1_000),
                usableFrom = Instant.parse("2023-01-01T00:00:00Z"),
                usableTo = Instant.parse("2025-12-31T23:59:59Z"),
            ),
        )
}
