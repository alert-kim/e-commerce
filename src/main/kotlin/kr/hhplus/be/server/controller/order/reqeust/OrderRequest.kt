package kr.hhplus.be.server.controller.order.reqeust

import java.math.BigDecimal

class OrderRequest(
    val userId: Long,
    val orderItems: List<OrderItem>,
    val totalPrice: BigDecimal,
) {
    data class OrderItem(
        val productId: Long,
        val quantity: Int,
        val price: BigDecimal,
    )
}
