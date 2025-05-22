package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.application.order.command.SendCompletedOrderFacadeCommand
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.command.SendOrderCompletedCommand
import org.springframework.stereotype.Service

@Service
class OrderSendFacade(
    private val orderService: OrderService
) {
    fun sendCompleted(command: SendCompletedOrderFacadeCommand) {
        val order = command.order
        val checkedOrder = order.checkCompleted()
        orderService.sendOrderCompleted(SendOrderCompletedCommand(checkedOrder))
    }
}
