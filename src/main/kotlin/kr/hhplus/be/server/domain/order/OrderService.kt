package kr.hhplus.be.server.domain.order

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.domain.order.command.*
import kr.hhplus.be.server.domain.order.dto.OrderSnapshot
import kr.hhplus.be.server.domain.order.event.OrderEvent
import kr.hhplus.be.server.domain.order.event.OrderEventRepository
import kr.hhplus.be.server.domain.order.event.OrderEventType
import kr.hhplus.be.server.domain.order.exception.NotFoundOrderException
import kr.hhplus.be.server.domain.order.result.CreateOrderResult
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class OrderService(
    private val repository: OrderRepository,
    private val eventRepository: OrderEventRepository,
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

    fun get(
        id: Long,
    ): Order =
        repository.findById(id)
            ?: throw NotFoundOrderException("by id: $id")
}
