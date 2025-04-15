package kr.hhplus.be.server.interfaces.coupon

import kr.hhplus.be.server.application.coupon.CouponFacade
import kr.hhplus.be.server.domain.user.exception.NotFoundUserException
import kr.hhplus.be.server.interfaces.ErrorCode
import kr.hhplus.be.server.interfaces.ErrorSpec
import kr.hhplus.be.server.interfaces.common.ServerApiResponse
import kr.hhplus.be.server.interfaces.common.handleRequest
import kr.hhplus.be.server.interfaces.coupon.request.IssueCouponRequest
import kr.hhplus.be.server.interfaces.coupon.response.CouponResponse
import kr.hhplus.be.server.interfaces.coupon.response.CouponSourcesResponse
import kr.hhplus.be.server.interfaces.coupon.response.CouponsResponse
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
    ): ResponseEntity<ServerApiResponse> = handleRequest(
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

    @GetMapping("/users/{userId}/coupons")
    override fun getUserCoupons(
        @PathVariable userId: Long,
    ) = handleRequest(
        block = {
            val coupons = couponFacade.getUserCoupons(userId)
            CouponsResponse.from(coupons)
        },
        errorSpec = {
            when (it) {
                is NotFoundUserException -> ErrorSpec.notFound(ErrorCode.NOT_FOUND_USER)
                else -> ErrorSpec.serverError(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        }
    )

    @PostMapping("/coupons:issue")
    override fun issueCoupon(
        @RequestBody request: IssueCouponRequest,
    ): CouponResponse =
        CouponResponse(
            id = 1L,
            userId = 2L,
            name = "쿠폰1",
            discountAmount = BigDecimal.valueOf(10_000),
            usedAt = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
}
