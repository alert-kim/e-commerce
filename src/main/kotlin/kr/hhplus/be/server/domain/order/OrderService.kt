package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.order.command.*
import kr.hhplus.be.server.domain.order.event.*
import kr.hhplus.be.server.domain.order.exception.NotFoundOrderException
import kr.hhplus.be.server.domain.order.repository.OrderEventRepository
import kr.hhplus.be.server.domain.order.repository.OrderRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class OrderService(
    private val repository: OrderRepository,
    private val eventRepository: OrderEventRepository,
    private val eventConsumerOffsetRepository: OrderEventConsumerOffsetRepository,
    private val client: OrderSnapshotClient,
) {
    fun createOrder(
        command: CreateOrderCommand,
    ): OrderId =
        repository.save(
            Order.new(
                userId = command.userId,
            )
        )

    fun placeStock(
        command: PlaceStockCommand,
    ) {
        val order = doGet(command.orderId.value)

        command.preparedProductForOrder.forEach {
            order.placeStock(
                productId = it.product.id,
                quantity = it.stock.quantity,
                unitPrice = it.product.price,
            )
        }

        repository.save(order)
    }

    fun applyCoupon(
        command: ApplyCouponCommand,
    ) {
        val order = doGet(command.orderId.value)

        order.applyCoupon(command.usedCoupon)

        repository.save(order)
    }

    fun pay(
        command: PayOrderCommand,
    ) {
        val payment = command.payment
        val order = doGet(payment.orderId.value)

        order.pay()
        val orderId = repository.save(order)
        val event = OrderEvent(
            orderId = orderId,
            type = OrderEventType.COMPLETED,
            snapshot = OrderSnapshot.from(order),
            createdAt = Instant.now(),
        )
        eventRepository.save(event)
    }

    fun sendOrderCompleted(
        command: SendOrderCompletedCommand,
    ) {
        val snapshot = command.orderSnapshot.checkCompleted()
        client.send(snapshot)
    }

    fun consumeEvent(
        command: ConsumeOrderEventCommand,
    ) {
        val offset = eventConsumerOffsetRepository.find(command.consumerId, command.event.type)
        when (offset) {
            null -> eventConsumerOffsetRepository.save(
                OrderEventConsumerOffset(
                    consumerId = command.consumerId,
                    value = command.event.id(),
                    eventType = command.event.type,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                )
            )

            else -> eventConsumerOffsetRepository.update(
                OrderEventConsumerOffset(
                    consumerId = command.consumerId,
                    value = command.event.id(),
                    eventType = command.event.type,
                    createdAt = offset.createdAt,
                    updatedAt = command.event.createdAt,
                )
            )
        }
    }

    fun get(
        id: Long,
    ): OrderView = OrderView.from(doGet(id))

    fun getAllEventsNotConsumedInOrder(
        consumerId: String,
        eventType: OrderEventType,
    ): List<OrderEvent> {
        val offset = eventConsumerOffsetRepository.find(
            consumerId = consumerId,
            eventType = eventType,
        )
        return when (offset) {
            null -> eventRepository.findAllByIdAsc()
            else -> eventRepository.findAllByIdGreaterThanOrderByIdAsc(
                id = offset.value,
            )
        }
    }

    private fun doGet(
        id: Long,
    ): Order =
        repository.findById(id)
            ?: throw NotFoundOrderException("by id: $id")
}
