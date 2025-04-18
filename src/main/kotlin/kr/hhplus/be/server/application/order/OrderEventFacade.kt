package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.application.order.command.OrderFacadeCommand
import kr.hhplus.be.server.domain.balance.BalanceService
import kr.hhplus.be.server.domain.balance.command.UseBalanceCommand
import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.command.UseCouponCommand
import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.order.OrderQueryModel
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.OrderSheet
import kr.hhplus.be.server.domain.order.command.ApplyCouponCommand
import kr.hhplus.be.server.domain.order.command.CreateOrderCommand
import kr.hhplus.be.server.domain.order.command.PayOrderCommand
import kr.hhplus.be.server.domain.order.command.PlaceStockCommand
import kr.hhplus.be.server.domain.order.event.OrderEvent
import kr.hhplus.be.server.domain.order.event.OrderEventType
import kr.hhplus.be.server.domain.payment.PaymentService
import kr.hhplus.be.server.domain.payment.command.PayCommand
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.product.command.AllocateStocksCommand
import kr.hhplus.be.server.domain.user.UserId
import kr.hhplus.be.server.domain.user.UserService
import org.springframework.stereotype.Service

@Service
class OrderEventFacade(
    private val balanceService: BalanceService,
    private val couponService: CouponService,
    private val orderService: OrderService,
    private val paymentService: PaymentService,
    private val productService: ProductService,
    private val userService: UserService,
) {

    fun order(
        command: OrderFacadeCommand,
    ): OrderQueryModel {
        val userId = verifyUser(command.userId)
        val orderId = createOrder(userId)
        val orderSheet = createOrderSheet(orderId, userId, command)
        placeProduct(orderSheet)
        pay(orderSheet)
        return orderService.get(orderId.value).let { OrderQueryModel.from(it) }
    }

    fun getAllEventsNotHandledInOrder(
        schedulerId: String,
        eventType: OrderEventType,
    ): List<OrderEvent> =
        orderService.getAllEventsNotHandledInOrder(schedulerId, eventType)

    private fun verifyUser(
        userId: Long,
    ): UserId =
        userService.get(userId).requireId()

    private fun placeProduct(
        sheet: OrderSheet,
    ) {
        val allocated = productService.allocateStocks(
            AllocateStocksCommand(
                needStocks = sheet.orderProducts.map {
                    AllocateStocksCommand.NeedStock(
                        productId = it.productId,
                        quantity = it.quantity,
                    )
                },
            )
        )
        orderService.placeStock(
            PlaceStockCommand(
                orderSheet = sheet,
                stocks = allocated.stocks,
            )
        )
    }

    private fun pay(
        orderSheet: OrderSheet,
    ) {
        if (orderSheet.couponId != null) {
            val coupon = couponService.use(UseCouponCommand(orderSheet.couponId, orderSheet.userId))
            orderService.applyCoupon(ApplyCouponCommand(orderSheet, coupon.value))
        }
        val order = orderService.get(orderSheet.orderId.value)

        balanceService.use(
            UseBalanceCommand(
                userId = order.userId,
                amount = order.totalAmount,
            )
        )

        val payment = paymentService.pay(
            PayCommand(
                userId = order.userId,
                orderId = order.requireId(),
                amount = order.totalAmount,
            )
        ).payment

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

    private fun createOrderSheet(
        orderId: OrderId,
        userId: UserId,
        command: OrderFacadeCommand,
    ): OrderSheet =
        with(command) {
            OrderSheet(
                orderId = orderId,
                userId = userId,
                orderProducts = orderProducts.map {
                    OrderSheet.OrderProduct(
                        productId = it.productId,
                        quantity = it.quantity,
                        unitPrice = it.unitPrice,
                        totalPrice = it.totalPrice,
                    )
                },
                couponId = couponId,
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                totalAmount = totalAmount,
            )
        }
}
