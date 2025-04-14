package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.order.command.*
import kr.hhplus.be.server.domain.order.exception.NotFoundOrderException
import kr.hhplus.be.server.domain.order.result.CreateOrderResult
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val repository: OrderRepository,
) {

    fun createOrder(
        command: CreateOrderCommand,
    ): CreateOrderResult {
        val orderId = repository.save(
            Order.new(
                userId = command.userId,
            )
        )
        return CreateOrderResult(
            orderId = orderId,
        )
    }

    fun placeStock(
        command: PlaceStockCommand,
    )  {
        val orderId = command.orderSheet.orderId
        val order = get(orderId.value)

        val stocks = command.stocks
        command.orderSheet.verifyProductPrice(stocks)

        order.placeStock(
            stocks = stocks,
        )
        repository.save(order)
    }

    fun applyCoupon(
        command: ApplyCouponCommand,
    ) {
        val orderId = command.orderSheet.orderId
        val order = get(orderId.value)
        command.orderSheet.verifyCoupon(command.coupon)

        order.applyCoupon(command.coupon)

        repository.save(order)
    }

    fun pay(
        command: PayOrderCommand,
    ) {
        val payment = command.payment
        val order = get(payment.orderId.value)

        order.pay()
        repository.save(order)
    }

    fun get(
        id: Long,
    ): Order =
        repository.findById(id)
            ?: throw NotFoundOrderException("by id: $id")
}
