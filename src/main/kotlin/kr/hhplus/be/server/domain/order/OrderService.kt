package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.order.command.*
import kr.hhplus.be.server.domain.order.event.*
import kr.hhplus.be.server.domain.order.exception.NotFoundOrderException
import kr.hhplus.be.server.domain.order.repository.OrderRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class OrderService(
    private val repository: OrderRepository,
    private val client: OrderSender,
    private val eventPublisher: OrderEventPublisher,
) {
    @Transactional
    fun createOrder(
        command: CreateOrderCommand,
    ): OrderId {
        val order = Order.new(
            userId = command.userId,
        )
        val saved = repository.save(order)
        eventPublisher.publish(OrderCreatedEvent.from(saved))
        return saved.id()
    }

    @Transactional
    fun placeStock(
        command: PlaceStockCommand,
    ) {
        val order = doGet(command.orderId.value)
        val orderProduct = order.placeStock(
            productId = command.product.id,
            quantity = command.stock.quantity,
            unitPrice = command.product.price,
        )
        eventPublisher.publish(OrderStockPlacedEvent.from(orderProduct))
    }

    @Transactional
    fun applyCoupon(
        command: ApplyCouponCommand,
    ) {
        val order = doGet(command.orderId.value)

        order.applyCoupon(command.usedCoupon)
        eventPublisher.publish(OrderCouponAppliedEvent.from(order))
    }

    @Transactional
    fun pay(
        command: PayOrderCommand,
    ) {
        val payment = command.payment
        val order = doGet(payment.orderId.value)

        order.pay()
        eventPublisher.publish(OrderCompletedEvent.from(order))
    }

    @Transactional
    fun failOrder(
        command: FailOrderCommand
    ) {
        val order = doGet(command.orderId.value)
        if (order.isFailed()) return

        order.fail()
        eventPublisher.publish(OrderFailedEvent.from(order))
    }

    @Transactional
    fun markFailHandled(command: MarkOrderFailHandledCommand) {
        val order = doGet(command.orderId.value)

        order.failHandled()
        eventPublisher.publish(OrderMarkedFailedHandledEvent.from(order))
    }

    @Transactional
    fun sendOrderCompleted(
        command: SendOrderCompletedCommand,
    ) {
        val order = command.order.checkCompleted()
        client.send(order)
    }

    fun get(
        id: Long,
    ): OrderView = OrderView.from(doGet(id))

    private fun doGet(
        id: Long,
    ): Order =
        repository.findById(id)
            ?: throw NotFoundOrderException("by id: $id")
}
