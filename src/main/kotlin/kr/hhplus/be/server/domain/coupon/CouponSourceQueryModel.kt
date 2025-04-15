package kr.hhplus.be.server.domain.coupon

import java.math.BigDecimal
import java.time.Instant

data class CouponSourceQueryModel(
    val id: CouponSourceId,
    val name: String,
    val status: CouponSourceStatus,
    val quantity: Int,
    val discountAmount: BigDecimal,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(
            couponSource: CouponSource,
        ): CouponSourceQueryModel =
            CouponSourceQueryModel(
                id = couponSource.requireId(),
                name = couponSource.name,
                status = couponSource.status,
                quantity = couponSource.quantity,
                discountAmount = couponSource.discountAmount,
                createdAt = couponSource.createdAt,
                updatedAt = couponSource.updatedAt,
            )
    }
}
