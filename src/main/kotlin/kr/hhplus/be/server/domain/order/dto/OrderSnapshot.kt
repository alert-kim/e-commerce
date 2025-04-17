package kr.hhplus.be.server.domain.order.dto

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderStatus
import java.math.BigDecimal
import java.time.Instant

data class OrderSnapshot(
    val id: Long,
    val userId: Long,
    val status: OrderStatus,
    val originalAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val couponId: Long?,
    val orderProducts: List<OrderProductSnapshot>,
    val createdAt: Instant,
    val updatedAt: Instant,
) {

    data class OrderProductSnapshot(
        val orderId: Long,
        val productId: Long,
        val quantity: Int,
        val unitPrice: BigDecimal,
        val totalPrice: BigDecimal,
        val createdAt: Instant,
    )

    companion object {
        fun from(order: Order): OrderSnapshot {
            return OrderSnapshot(
                id = order.requireId().value,
                userId = order.userId.value,
                status = order.status,
                originalAmount = order.originalAmount,
                discountAmount = order.discountAmount,
                totalAmount = order.totalAmount,
                couponId = order.couponId?.value,
                orderProducts = order.products.map { product ->
                    OrderProductSnapshot(
                        orderId = product.orderId.value,
                        productId = product.productId.value,
                        quantity = product.quantity,
                        unitPrice = product.unitPrice,
                        totalPrice = product.totalPrice,
                        createdAt = product.createdAt,
                    )
                },
                createdAt = order.createdAt,
                updatedAt = order.updatedAt,
            )
        }
    }
}
