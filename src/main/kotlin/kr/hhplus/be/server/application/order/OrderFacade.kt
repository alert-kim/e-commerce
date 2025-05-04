package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.application.order.command.ApplyCouponProcessorCommand
import kr.hhplus.be.server.application.order.command.CreateOrderProcessorCommand
import kr.hhplus.be.server.application.order.command.OrderFacadeCommand
import kr.hhplus.be.server.application.order.command.PayOrderProcessorCommand
import kr.hhplus.be.server.application.order.command.PlaceOrderProductProcessorCommand
import kr.hhplus.be.server.application.order.result.OrderFacadeResult
import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.order.OrderService
import org.springframework.stereotype.Service

@Service
class OrderFacade(
    private val orderService: OrderService,
    private val orderCreationProcessor: OrderCreationProcessor,
    private val orderProductProcessor: OrderProductProcessor,
    private val orderCouponProcessor: OrderCouponProcessor,
    private val orderPaymentProcessor: OrderPaymentProcessor,
) {
    fun order(
        command: OrderFacadeCommand,
    ): OrderFacadeResult {
        command.validate()
        val orderId = createOrder(command.userId)
        placeStocks(orderId, command)
        applyCoupon(orderId, command)
        pay(orderId)
        return OrderFacadeResult(orderService.get(orderId.value))
    }

    private fun createOrder(userId: Long) =
        orderCreationProcessor.createOrder(
            CreateOrderProcessorCommand(userId)
        ).orderId

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
        command: OrderFacadeCommand,
    ) {
        val couponId = command.couponId ?: return
        val order = orderService.get(orderId.value)
        orderCouponProcessor.applyCouponToOrder(
            ApplyCouponProcessorCommand(orderId, order.userId, couponId)
        )
    }

    private fun pay(
        orderId: OrderId,
    ) {
        val order = orderService.get(orderId.value)
        orderPaymentProcessor.processPayment(PayOrderProcessorCommand(orderId, order.userId, order.totalAmount))
    }
}
