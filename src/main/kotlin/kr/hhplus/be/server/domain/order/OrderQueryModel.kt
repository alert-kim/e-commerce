package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.coupon.CouponId
import java.math.BigDecimal
import java.time.Instant

data class OrderQueryModel (
    val id: OrderId,
    val userId: kr.hhplus.be.server.domain.user.UserId,
    val status: OrderStatus,
    val couponId: CouponId?,
    val originalAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val products: List<OrderProductQueryModel>,
    val createdAt: Instant,
    val updatedAt: Instant,
)
