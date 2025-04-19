package kr.hhplus.be.server.application.order.command

import java.math.BigDecimal

data class OrderFacadeCommand (
    val userId: Long,
    val orderProducts: List<OrderProduct>,
    val couponId: Long? = null,
    val originalAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val totalAmount: BigDecimal,
) {
    data class OrderProduct(
        val productId: Long,
        val quantity: Int,
        val unitPrice: BigDecimal,
        val totalPrice: BigDecimal,
    )
}
