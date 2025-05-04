package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.application.order.command.*
import kr.hhplus.be.server.application.order.result.OrderFacadeResult
import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.order.OrderService
import org.springframework.stereotype.Service

@Service
class OrderFacade(
    private val orderService: OrderService,
    private val orderLifecycleProcessor: OrderLifecycleProcessor,
    private val orderProductProcessor: OrderProductProcessor,
    private val orderCouponProcessor: OrderCouponProcessor,
    private val orderPaymentProcessor: OrderPaymentProcessor,
) {
    fun order(
        command: OrderFacadeCommand,
    ): OrderFacadeResult {
        command.validate()
        val orderId = createOrder(command.userId)

        runCatching {
            placeStocks(orderId, command)
            applyCoupon(orderId, command)
            pay(orderId)
        }.onFailure { exception ->
            orderLifecycleProcessor.failOrder(FailOrderProcessorCommand(
                orderId = orderId,
                reason = exception.message,
            ))
            throw exception
        }

        return OrderFacadeResult(orderService.get(orderId.value))
    }

    private fun createOrder(userId: Long) =
        orderLifecycleProcessor.createOrder(
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
