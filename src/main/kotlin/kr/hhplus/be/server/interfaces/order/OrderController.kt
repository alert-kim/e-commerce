package kr.hhplus.be.server.interfaces.order

import kr.hhplus.be.server.application.order.OrderFacade
import kr.hhplus.be.server.application.order.command.OrderFacadeCommand
import kr.hhplus.be.server.domain.balance.exception.BelowMinBalanceAmountException
import kr.hhplus.be.server.domain.balance.exception.InsufficientBalanceException
import kr.hhplus.be.server.domain.coupon.exception.AlreadyUsedCouponException
import kr.hhplus.be.server.domain.coupon.exception.ExpiredCouponException
import kr.hhplus.be.server.domain.coupon.exception.NotFoundCouponException
import kr.hhplus.be.server.domain.coupon.exception.NotOwnedCouponException
import kr.hhplus.be.server.domain.order.exception.InvalidOrderPriceException
import kr.hhplus.be.server.domain.order.exception.InvalidOrderProductQuantityException
import kr.hhplus.be.server.domain.product.excpetion.NotFoundProductException
import kr.hhplus.be.server.domain.stock.exception.InvalidStockQuantityToAllocateException
import kr.hhplus.be.server.domain.stock.exception.OutOfStockException
import kr.hhplus.be.server.domain.user.exception.NotFoundUserException
import kr.hhplus.be.server.interfaces.ErrorCode
import kr.hhplus.be.server.interfaces.ErrorSpec
import kr.hhplus.be.server.interfaces.common.handleRequest
import kr.hhplus.be.server.interfaces.order.reqeust.OrderRequest
import kr.hhplus.be.server.interfaces.order.response.OrderResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderFacade: OrderFacade,
) : OrderControllerInterface {

    @PostMapping("")
    override fun order(
        @RequestBody request: OrderRequest,
    ) =
        handleRequest(
            block = {
                val result = orderFacade.order(
                    OrderFacadeCommand(
                        userId = request.userId,
                        productsToOrder = request.orderProducts.map {
                            OrderFacadeCommand.ProductToOrder(
                                productId = it.productId,
                                quantity = it.quantity,
                                unitPrice = it.unitPrice,
                                totalPrice = it.totalPrice,
                            )
                        },
                        couponId = request.couponId,
                        originalAmount = request.originalAmount,
                        discountAmount = request.discountAmount,
                        totalAmount = request.totalAmount,
                    )
                )

                OrderResponse.from(result.value)
            },
            errorSpec = {
                when (it) {
                    is OutOfStockException -> ErrorSpec.badRequest(ErrorCode.OUT_OF_STOCK_PRODUCT)
                    is InvalidOrderPriceException -> ErrorSpec.badRequest(ErrorCode.INVALID_ORDER_PRICE)
                    is InvalidOrderProductQuantityException,
                    is InvalidStockQuantityToAllocateException -> ErrorSpec.badRequest(ErrorCode.INVALID_ORDER_QUANTITY)

                    is AlreadyUsedCouponException -> ErrorSpec.badRequest(ErrorCode.ALREADY_USED_COUPON)
                    is ExpiredCouponException -> ErrorSpec.badRequest(ErrorCode.EXPIRED_COUPON)
                    is BelowMinBalanceAmountException -> ErrorSpec.badRequest(ErrorCode.INSUFFICIENT_BALANCE)
                    is NotOwnedCouponException -> ErrorSpec.forbidden(ErrorCode.NOT_OWNED_COUPON)
                    is NotFoundUserException -> ErrorSpec.notFound(ErrorCode.NOT_FOUND_USER)
                    is NotFoundProductException -> ErrorSpec.notFound(ErrorCode.NOT_FOUND_PRODUCT)
                    is NotFoundCouponException -> ErrorSpec.notFound(ErrorCode.NOT_FOUND_COUPON)
                    else -> throw IllegalStateException("Unknown error occurred", it)
                }
            }
        )
}
