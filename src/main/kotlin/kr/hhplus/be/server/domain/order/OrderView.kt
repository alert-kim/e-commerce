package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.coupon.CouponId
import kr.hhplus.be.server.domain.order.exception.InvalidOrderStatusException
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

data class OrderView(
    val id: OrderId,
    val userId: UserId,
    val status: OrderStatus,
    val couponId: CouponId?,
    val originalAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val products: List<OrderProductView>,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun isFailed(): Boolean = status == OrderStatus.FAILED

    fun getOrNullCompletedAt(): Instant? = if (status == OrderStatus.COMPLETED) updatedAt else null

    fun checkCompleted(): OrderView {
        if (status != OrderStatus.COMPLETED) {
            throw InvalidOrderStatusException(
                id = id,
                expect = OrderStatus.COMPLETED,
                status = status,
            )
        }
        return this
    }

    companion object {
        fun from(order: Order): OrderView =
            OrderView(
                id = order.id(),
                userId = order.userId,
                status = order.status,
                couponId = order.couponId,
                originalAmount = order.originalAmount,
                discountAmount = order.discountAmount,
                totalAmount = order.totalAmount,
                products = order.products.map { OrderProductView.from(it) },
                createdAt = order.createdAt,
                updatedAt = order.updatedAt
            )
    }
}
