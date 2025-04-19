package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.application.order.command.ConsumeOrderEventsFacadeCommand
import kr.hhplus.be.server.application.order.command.OrderFacadeCommand
import kr.hhplus.be.server.application.order.command.SendOrderFacadeCommand
import kr.hhplus.be.server.domain.balance.BalanceService
import kr.hhplus.be.server.domain.balance.command.UseBalanceCommand
import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.command.UseCouponCommand
import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.order.OrderView
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.command.ApplyCouponCommand
import kr.hhplus.be.server.domain.order.command.CreateOrderCommand
import kr.hhplus.be.server.domain.order.command.PayOrderCommand
import kr.hhplus.be.server.domain.order.command.PlaceStockCommand
import kr.hhplus.be.server.domain.order.event.OrderEvent
import kr.hhplus.be.server.domain.order.event.OrderEventType
import kr.hhplus.be.server.domain.order.command.*
import kr.hhplus.be.server.domain.payment.PaymentService
import kr.hhplus.be.server.domain.payment.command.PayCommand
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.product.command.AllocateStocksCommand
import kr.hhplus.be.server.domain.user.UserId
import kr.hhplus.be.server.domain.user.UserService
import org.springframework.stereotype.Service

@Service
class OrderFacade(
    private val balanceService: BalanceService,
    private val couponService: CouponService,
    private val orderService: OrderService,
    private val paymentService: PaymentService,
    private val productService: ProductService,
    private val userService: UserService,
) {
    fun order(
        command: OrderFacadeCommand,
    ): OrderView {
        val userId = verifyUser(command.userId)
        val orderId = createOrder(userId)
        placeProduct(orderId,command)
        applyCoupon(orderId, command)
        pay(orderId)
        return orderService.get(orderId.value)
    }

    fun sendOrderCompletionData(
        command: SendOrderFacadeCommand,
    ) {
       orderService.sendOrderCompleted(SendOrderCompletedCommand(command.orderSnapshot))
    }

    fun consumeEvent(
        command: ConsumeOrderEventsFacadeCommand,
    ) {
        orderService.consumeEvent(ConsumeOrderEventCommand.of(command.consumerId, command.events))
    }

    fun getAllEventsNotConsumedInOrder(
        consumerId: String,
        eventType: OrderEventType,
    ): List<OrderEvent> =
        orderService.getAllEventsNotConsumedInOrder(
            consumerId = consumerId,
            eventType = eventType,
        )

    private fun verifyUser(
        userId: Long,
    ): UserId =
        userService.get(userId).id

    private fun placeProduct(
        orderId: OrderId,
        command: OrderFacadeCommand,
    ) {
        val allocated = productService.allocateStocks(
            AllocateStocksCommand(
                needStocks = command.orderProducts.map {
                    AllocateStocksCommand.NeedStock(
                        productId = it.productId,
                        quantity = it.quantity,
                    )
                },
            )
        )
        orderService.placeStock(
            PlaceStockCommand(
                orderId = orderId,
                stocks = allocated.stocks,
            )
        )
    }

    private fun applyCoupon(
        orderId: OrderId,
        command: OrderFacadeCommand,
    ) {
        if (command.couponId != null) {
            val usedCoupon = couponService.use(
                UseCouponCommand(command.couponId, UserId(command.userId))
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
        ).orderId
}
