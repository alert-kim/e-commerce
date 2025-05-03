package kr.hhplus.be.server.application.order.command

import kr.hhplus.be.server.domain.order.OrderId
import java.math.BigDecimal

data class PlaceOrderProductProcessorCommand(
    val orderId: OrderId,
    val productId: Long,
    val quantity: Int,
    val unitPrice: BigDecimal,
) {
    companion object {
        fun of(command: OrderFacadeCommand.ProductToOrder, orderId: OrderId): PlaceOrderProductProcessorCommand =
            PlaceOrderProductProcessorCommand(
                orderId = orderId,
                productId = command.productId,
                quantity = command.quantity,
                unitPrice = command.unitPrice,
            )
    }
}
