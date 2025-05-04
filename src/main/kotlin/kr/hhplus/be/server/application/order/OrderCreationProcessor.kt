package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.application.order.command.CreateOrderProcessorCommand
import kr.hhplus.be.server.application.order.result.OrderCreationProcessorResult
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.command.CreateOrderCommand
import kr.hhplus.be.server.domain.user.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderCreationProcessor(
    private val userService: UserService,
    private val orderService: OrderService,
) {

    @Transactional
    fun createOrder(command: CreateOrderProcessorCommand): OrderCreationProcessorResult {
        val user = userService.get(command.userId)
        val orderId = orderService.createOrder(
            command = CreateOrderCommand(
                userId = user.id,
            ),
        )
        return OrderCreationProcessorResult(orderId)
    }
}
