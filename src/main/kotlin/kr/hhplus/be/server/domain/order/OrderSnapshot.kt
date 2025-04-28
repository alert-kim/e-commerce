package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.order.exception.InvalidOrderStatusException
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

    fun checkCompleted(): OrderSnapshot {
        if (status != OrderStatus.COMPLETED) {
            throw InvalidOrderStatusException(
                id = OrderId(id),
                expect = OrderStatus.COMPLETED,
                status = status,
            )
        }
        return this
    }

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
