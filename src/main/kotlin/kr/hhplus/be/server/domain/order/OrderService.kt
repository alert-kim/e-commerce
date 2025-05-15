package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.application.order.command.MarkOrderFailHandledProcessorCommand
import kr.hhplus.be.server.domain.order.command.*
import kr.hhplus.be.server.domain.order.event.OrderCompletedEvent
import kr.hhplus.be.server.domain.order.event.OrderFailedEvent
import kr.hhplus.be.server.domain.order.exception.NotFoundOrderException
import kr.hhplus.be.server.domain.order.repository.OrderRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional(readOnly = true)
class OrderService(
    private val repository: OrderRepository,
    private val client: OrderSnapshotClient,
    private val publisher: ApplicationEventPublisher,
) {
    @Transactional
    fun createOrder(
        command: CreateOrderCommand,
    ): OrderId =
        repository.save(
            Order.new(
                userId = command.userId,
            )
        ).id()

    @Transactional
    fun placeStock(
        command: PlaceStockCommand,
    ) {
        val order = doGet(command.orderId.value)

        order.placeStock(
            productId = command.product.id,
            quantity = command.stock.quantity,
            unitPrice = command.product.price,
        )
    }

    @Transactional
    fun applyCoupon(
        command: ApplyCouponCommand,
    ) {
        val order = doGet(command.orderId.value)

        order.applyCoupon(command.usedCoupon)
    }

    @Transactional
    fun pay(
        command: PayOrderCommand,
    ) {
        val payment = command.payment
        val order = doGet(payment.orderId.value)

        order.pay()
        publisher.publishEvent(
            OrderCompletedEvent(
                orderId = order.id(),
                snapshot = OrderSnapshot.from(order),
                createdAt = Instant.now(),
            )
        )
    }

    @Transactional
    fun failOrder(
        command: FailOrderCommand
    ) {
        val order = doGet(command.orderId.value)
        if (order.isFailed()) return

        order.fail()
        val event = OrderFailedEvent(
            orderId = order.id(),
            snapshot = OrderSnapshot.from(order),
            createdAt = Instant.now(),
        )
        publisher.publishEvent(event)
    }


    @Transactional
    fun markFailHandled(command: MarkOrderFailHandledCommand) {
        val order = doGet(command.orderId.value)

        order.failHandled()
    }


    @Transactional
    fun sendOrderCompleted(
        command: SendOrderCompletedCommand,
    ) {
        val snapshot = command.orderSnapshot.checkCompleted()
        client.send(snapshot)
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
