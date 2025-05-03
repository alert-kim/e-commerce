package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.application.order.command.ApplyCouponProcessorCommand
import kr.hhplus.be.server.application.order.command.OrderFacadeCommand
import kr.hhplus.be.server.application.order.command.PayOrderProcessorCommand
import kr.hhplus.be.server.application.order.command.PlaceOrderProductProcessorCommand
import kr.hhplus.be.server.application.order.result.OrderFacadeResult
import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.command.CreateOrderCommand
import kr.hhplus.be.server.domain.user.UserId
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.domain.user.UserView
import org.springframework.stereotype.Service

@Service
class OrderFacade(
    private val orderService: OrderService,
    private val userService: UserService,
    private val orderProductProcessor: OrderProductProcessor,
    private val orderCouponProcessor: OrderCouponProcessor,
    private val orderPaymentProcessor: OrderPaymentProcessor,
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
        val couponId = command.couponId ?: return
        orderCouponProcessor.applyCouponToOrder(
            ApplyCouponProcessorCommand(orderId, userId, couponId)
        )
    }

    private fun pay(
        orderId: OrderId,
    ) {
        val order = orderService.get(orderId.value)
        orderPaymentProcessor.processPayment(PayOrderProcessorCommand(orderId, order.userId, order.totalAmount))
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
