package kr.hhplus.be.server.interfaces.order.reqeust

import java.math.BigDecimal

class OrderRequest(
    val userId: Long,
    val orderProducts: List<OrderProductRequest>,
    val couponId: Long? = null,
    val originalAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val totalAmount: BigDecimal,
) {
    data class OrderProductRequest(
        val productId: Long,
        val quantity: Int,
        val unitPrice: BigDecimal,
        val totalPrice: BigDecimal,
    )
}
