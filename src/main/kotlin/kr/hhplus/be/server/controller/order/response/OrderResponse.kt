package kr.hhplus.be.server.controller.order.response

import kr.hhplus.be.server.model.order.OrderStatus
import java.math.BigDecimal
import java.time.Instant

class OrderResponse(
    val id: Long,
    val userId: Long,
    val status: OrderStatus,
    val totalPrice: BigDecimal,
    val expiresAt: Instant,
    val orderItems: List<OrderItem>,
    val createdAt: Instant,
) {
    data class OrderItem(
        val productId: Long,
        val quantity: Int,
        val price: BigDecimal,
    )
}
