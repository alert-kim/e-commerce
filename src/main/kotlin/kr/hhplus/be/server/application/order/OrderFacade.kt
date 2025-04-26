package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.application.order.command.ConsumeOrderEventsFacadeCommand
import kr.hhplus.be.server.application.order.command.OrderFacadeCommand
import kr.hhplus.be.server.application.order.command.SendOrderFacadeCommand
import kr.hhplus.be.server.application.order.result.OrderResult
import kr.hhplus.be.server.domain.balance.BalanceService
import kr.hhplus.be.server.domain.balance.command.UseBalanceCommand
import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.command.UseCouponCommand
import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.OrderView
import kr.hhplus.be.server.domain.order.command.*
import kr.hhplus.be.server.domain.order.event.OrderEvent
import kr.hhplus.be.server.domain.order.event.OrderEventType
import kr.hhplus.be.server.domain.payment.PaymentService
import kr.hhplus.be.server.domain.payment.command.PayCommand
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.stock.StockService
import kr.hhplus.be.server.domain.stock.command.AllocateStocksCommand
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
    private val productService: ProductService,
    private val userService: UserService,
    private val stockService: StockService,
) {
    fun order(
        command: OrderFacadeCommand,
    ): OrderResult.Single {
        command.validate()
        val userId = getUser(command.userId).id
        val orderId = createOrder(userId)
        placeProduct(orderId, command)
        applyCoupon(orderId, userId, command)
        pay(orderId)
        return OrderResult.Single(orderService.get(orderId.value))
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
    ): OrderResult.Events =
        OrderResult.Events(
            orderService.getAllEventsNotConsumedInOrder(
                consumerId = consumerId,
                eventType = eventType,
            )
        )

    private fun getUser(
        userId: Long,
    ): UserView =
        userService.get(userId)

    private fun placeProduct(
        orderId: OrderId,
        command: OrderFacadeCommand,
    ) {
        val products = productService.getAllByIds(command.orderProductIds())

        val purchasableProducts = command.productsToOrder.map { orderProduct ->
            products.validatePurchasable(
                orderProduct.productId,
                orderProduct.unitPrice,
            )
        }

        val stocksAllocated = stockService.allocate(AllocateStocksCommand(
            purchasableProducts.associate {
                it.id to command.quantityOfProduct(it.id)
            }
        ))

        orderService.placeStock(PlaceStockCommand.of(orderId, purchasableProducts, stocksAllocated.stocks))
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
