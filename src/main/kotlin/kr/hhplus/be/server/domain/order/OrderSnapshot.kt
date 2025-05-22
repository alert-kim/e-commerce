package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.coupon.CouponId
import kr.hhplus.be.server.domain.order.exception.InvalidOrderStatusException
import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

data class OrderSnapshot(
    val id: OrderId,
    val userId: UserId,
    val status: OrderStatus,
    val originalAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val couponId: CouponId?,
    val orderProducts: List<OrderProductSnapshot>,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val completedAt: Instant
        get() =updatedAt

    fun checkCompleted(): OrderSnapshot {
        if (status != OrderStatus.COMPLETED) {
            throw InvalidOrderStatusException(
                id = id,
                expect = OrderStatus.COMPLETED,
                status = status,
            )
        }
        return this
    }

    data class OrderProductSnapshot(
        val productId: ProductId,
        val quantity: Int,
        val unitPrice: BigDecimal,
        val totalPrice: BigDecimal,
        val createdAt: Instant,
    )

    companion object {
        fun from(order: Order): OrderSnapshot =
            OrderSnapshot(
                id = order.id(),
                userId = order.userId,
                status = order.status,
                originalAmount = order.originalAmount,
                discountAmount = order.discountAmount,
                totalAmount = order.totalAmount,
                couponId = order.couponId,
                orderProducts = order.products.map { product ->
                    OrderProductSnapshot(
                        productId = product.productId,
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
