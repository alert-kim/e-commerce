package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.order.command.*
import kr.hhplus.be.server.domain.order.event.*
import kr.hhplus.be.server.domain.order.exception.NotFoundOrderException
import kr.hhplus.be.server.domain.order.repository.OrderEventRepository
import kr.hhplus.be.server.domain.order.repository.OrderRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional(readOnly = true)
class OrderService(
    private val repository: OrderRepository,
    private val eventRepository: OrderEventRepository,
    private val eventConsumerOffsetRepository: OrderEventConsumerOffsetRepository,
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

        command.preparedProductForOrder.forEach {
            order.placeStock(
                productId = it.product.id,
                quantity = it.stock.quantity,
                unitPrice = it.product.price,
            )
        }
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
        val event = OrderCompletedEvent(
            orderId = order.id(),
            snapshot = OrderSnapshot.from(order),
            createdAt = Instant.now(),
        )
        publisher.publishEvent(event)
    }

    @Transactional
    fun sendOrderCompleted(
        command: SendOrderCompletedCommand,
    ) {
        val snapshot = command.orderSnapshot.checkCompleted()
        client.send(snapshot)
    }

    @Transactional
    fun consumeEvent(
        command: ConsumeOrderEventCommand,
    ) {
        val offset =
            eventConsumerOffsetRepository.find(OrderEventConsumerOffsetId(command.consumerId, command.event.type))
        when (offset) {
            null -> eventConsumerOffsetRepository.save(
                OrderEventConsumerOffset.new(
                    consumerId = command.consumerId,
                    eventId = command.event.id(),
                    eventType = command.event.type,
                )
            )

            else -> offset.update(
                eventId = command.event.id(),
            )
        }
    }

    fun get(
        id: Long,
    ): OrderView = OrderView.from(doGet(id))

    fun getAllEventsNotConsumedInOrder(
        consumerId: String,
        eventType: OrderEventType,
    ): List<OrderJpaEvent> {
        val offset = eventConsumerOffsetRepository.find(
            OrderEventConsumerOffsetId(
                consumerId = consumerId,
                eventType = eventType,
            )
        )
        return when (offset) {
            null -> eventRepository.findAllByIdAsc()
            else -> eventRepository.findAllByIdGreaterThanOrderByIdAsc(
                id = offset.eventId,
            )
        }
    }

    private fun doGet(
        id: Long,
    ): Order =
        repository.findById(id)
            ?: throw NotFoundOrderException("by id: $id")
}
