package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.coupon.CouponId
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

data class OrderQueryModel (
    val id: OrderId,
    val userId: UserId,
    val status: OrderStatus,
    val couponId: CouponId?,
    val originalAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val products: List<OrderProductQueryModel>,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(order: Order): OrderQueryModel {
            return OrderQueryModel(
                id = order.id,
                userId = order.userId,
                status = order.status,
                couponId = order.couponId,
                originalAmount = order.originalAmount,
                discountAmount = order.discountAmount,
                totalAmount = order.totalAmount,
                products = order.products.map { OrderProductQueryModel.from(it) },
                createdAt = order.createdAt,
                updatedAt = order.updatedAt
            )
        }
    }
}
