package kr.hhplus.be.server.interfaces.coupon.api

import kr.hhplus.be.server.application.coupon.CouponFacade
import kr.hhplus.be.server.application.coupon.command.IssueCouponFacadeCommand
import kr.hhplus.be.server.domain.coupon.exception.NotFoundCouponSourceException
import kr.hhplus.be.server.domain.coupon.exception.OutOfStockCouponSourceException
import kr.hhplus.be.server.domain.user.exception.NotFoundUserException
import kr.hhplus.be.server.interfaces.ErrorCode
import kr.hhplus.be.server.interfaces.ErrorSpec
import kr.hhplus.be.server.interfaces.common.api.ServerApiResponse
import kr.hhplus.be.server.interfaces.common.api.handleRequest
import kr.hhplus.be.server.interfaces.coupon.api.request.IssueCouponRequest
import kr.hhplus.be.server.interfaces.coupon.api.response.CouponResponse
import kr.hhplus.be.server.interfaces.coupon.api.response.CouponSourcesResponse
import kr.hhplus.be.server.interfaces.coupon.api.response.CouponsResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
class CouponController(
    private val couponFacade: CouponFacade,
) : CouponControllerInterface {

    @PostMapping("/coupons")
    override fun issueCoupon(
        @RequestBody request: IssueCouponRequest,
    ) = handleRequest(
        block = {
            val result = couponFacade.issueCoupon(
                IssueCouponFacadeCommand(
                    couponSourceId = request.couponSourceId,
                    userId = request.userId,
                )
            )
            CouponResponse.from(result)
        },
        errorSpec = {
            when (it) {
                is OutOfStockCouponSourceException -> ErrorSpec.badRequest(ErrorCode.OUT_OF_STOCK_COUPON_SOURCE)
                is NotFoundUserException -> ErrorSpec.notFound(ErrorCode.NOT_FOUND_USER)
                is NotFoundCouponSourceException -> ErrorSpec.notFound(ErrorCode.NOT_FOUND_COUPON_SOURCE)
                else -> ErrorSpec.serverError(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        }
    )

    @GetMapping("/users/{userId}/coupons")
    override fun getUserCoupons(
        @PathVariable userId: Long,
    ) = handleRequest(
        block = {
            val coupons = couponFacade.getUsableCoupons(userId)
            CouponsResponse.Companion.from(coupons)
        },
        errorSpec = {
            when (it) {
                is NotFoundUserException -> ErrorSpec.notFound(ErrorCode.NOT_FOUND_USER)
                else -> ErrorSpec.serverError(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        }
    )

    @GetMapping("/couponSources")
    override fun getCouponSources(
    ): ResponseEntity<ServerApiResponse> = handleRequest(
        block = {
            val coupons = couponFacade.getAllSourcesIssuable()
            CouponSourcesResponse.Companion.from(coupons)
        },
        errorSpec = {
            when (it) {
                else -> ErrorSpec.serverError(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        }
    )
}
