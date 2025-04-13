package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.application.order.command.OrderFacadeCommand
import kr.hhplus.be.server.domain.order.OrderQueryModel
import org.springframework.stereotype.Service

@Service
class OrderFacade(
) {
    fun order(
        command: OrderFacadeCommand,
    ): OrderQueryModel = TODO()
}
