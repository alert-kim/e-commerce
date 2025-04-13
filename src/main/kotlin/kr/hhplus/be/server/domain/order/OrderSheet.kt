package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal

data class OrderSheet(
    val orderId: OrderId,
    val userId: UserId,
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
