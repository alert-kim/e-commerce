package kr.hhplus.be.server.domain.order

import java.math.BigDecimal
import java.time.Instant

data class OrderItemResult(
    val orderId: Long,
    val productId: Long,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal,
    val createdAt: Instant,
) {
    companion object {
        fun from(orderProductSnapshot: OrderSnapshot.OrderProductSnapshot): OrderItemResult {
            return OrderItemResult(
                orderId = orderProductSnapshot.orderId,
                productId = orderProductSnapshot.productId,
                quantity = orderProductSnapshot.quantity,
                unitPrice = orderProductSnapshot.unitPrice,
                totalPrice = orderProductSnapshot.totalPrice,
                createdAt = orderProductSnapshot.createdAt,
            )
        }
    }
} 