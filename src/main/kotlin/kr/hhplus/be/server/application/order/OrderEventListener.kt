package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.command.SendOrderCompletedCommand
import kr.hhplus.be.server.domain.order.event.OrderCompletedEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class OrderEventListener(
    private val orderService: OrderService,
) {
    @TransactionalEventListener
    fun handle(event: OrderCompletedEvent) {
        orderService.sendOrderCompleted(SendOrderCompletedCommand(event.snapshot))
    }
}
