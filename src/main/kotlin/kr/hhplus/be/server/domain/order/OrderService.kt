package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.order.command.*
import kr.hhplus.be.server.domain.order.dto.OrderSnapshot
import kr.hhplus.be.server.domain.order.event.*
import kr.hhplus.be.server.domain.order.exception.NotFoundOrderException
import kr.hhplus.be.server.domain.order.result.CreateOrderResult
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
    ) {
        val order = get(command.orderId.value)

        order.placeStock(
            stocks = command.stocks,
        )
        repository.save(order)
    }

    fun applyCoupon(
        command: ApplyCouponCommand,
    ) {
        val order = get(command.orderId.value)

        order.applyCoupon(command.coupon)

        repository.save(order)
    }

    fun pay(
        command: PayOrderCommand,
    ) {
        val payment = command.payment
        val order = get(payment.orderId.value)

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
        when(offset) {
            null -> eventConsumerOffsetRepository.save(OrderEventConsumerOffset(
                consumerId = command.consumerId,
                value = command.event.requireId(),
                eventType = command.event.type,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            ))
            else -> eventConsumerOffsetRepository.update(OrderEventConsumerOffset(
                consumerId = command.consumerId,
                value = command.event.requireId(),
                eventType = command.event.type,
                createdAt = offset.createdAt,
                updatedAt = command.event.createdAt,
            ))
        }
    }

    fun get(
        id: Long,
    ): Order =
        repository.findById(id)
            ?: throw NotFoundOrderException("by id: $id")

    fun getAllEventsNotConsumedInOrder(
        consumerId: String,
        eventType: OrderEventType,
    ): List<OrderEvent> {
        val offset = eventConsumerOffsetRepository.find(
            consumerId = consumerId,
            eventType = eventType,
        )
        return when(offset) {
            null -> eventRepository.findAllOrderByIdAsc()
            else -> eventRepository.findAllByIdGreaterThanOrderByIdAsc(
                id = offset.value,
            )
        }
    }
}
