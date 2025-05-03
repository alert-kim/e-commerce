package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.application.order.command.OrderFacadeCommand
import kr.hhplus.be.server.application.order.command.PlaceOrderProductProcessorCommand
import kr.hhplus.be.server.application.order.result.OrderFacadeResult
import kr.hhplus.be.server.domain.balance.BalanceService
import kr.hhplus.be.server.domain.balance.command.UseBalanceCommand
import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.command.UseCouponCommand
import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.command.ApplyCouponCommand
import kr.hhplus.be.server.domain.order.command.CreateOrderCommand
import kr.hhplus.be.server.domain.order.command.PayOrderCommand
import kr.hhplus.be.server.domain.payment.PaymentService
import kr.hhplus.be.server.domain.payment.command.PayCommand
import kr.hhplus.be.server.domain.user.UserId
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.domain.user.UserView
import org.springframework.stereotype.Service

@Service
class OrderFacade(
    private val balanceService: BalanceService,
    private val couponService: CouponService,
    private val orderService: OrderService,
    private val paymentService: PaymentService,
    private val userService: UserService,
    private val orderProductProcessor: OrderProductProcessor,
) {
    fun order(
        command: OrderFacadeCommand,
    ): OrderFacadeResult {
        command.validate()
        val userId = getUser(command.userId).id
        val orderId = createOrder(userId)
        placeStocks(orderId, command)
        applyCoupon(orderId, userId, command)
        pay(orderId)
        return OrderFacadeResult(orderService.get(orderId.value))
    }

    private fun getUser(
        userId: Long,
    ): UserView =
        userService.get(userId)

    private fun placeStocks(
        orderId: OrderId,
        command: OrderFacadeCommand,
    ) {
        command.productsToOrder
            .sortedBy { it.productId }
            .forEach {
                orderProductProcessor.placeOrderProduct(
                    PlaceOrderProductProcessorCommand.of(it, orderId)
                )
            }
    }

    private fun applyCoupon(
        orderId: OrderId,
        userId: UserId,
        command: OrderFacadeCommand,
    ) {
        if (command.couponId != null) {
            val usedCoupon = couponService.use(
                UseCouponCommand(command.couponId, userId)
            )
            orderService.applyCoupon(ApplyCouponCommand(orderId, usedCoupon))
        }
    }

    private fun pay(
        orderId: OrderId,
    ) {
        val order = orderService.get(orderId.value)

        val usedAmount = balanceService.use(
            UseBalanceCommand(
                userId = order.userId,
                amount = order.totalAmount,
            )
        )

        val payment = paymentService.pay(
            PayCommand(
                userId = order.userId,
                orderId = order.id,
                amount = usedAmount,
            )
        )

        orderService.pay(PayOrderCommand(payment))
    }

    private fun createOrder(
        userId: UserId,
    ): OrderId =
        orderService.createOrder(
            command = CreateOrderCommand(
                userId = userId,
            ),
        )
}
