package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.coupon.exception.RequiredCouponSourceIdException
import java.math.BigDecimal
import java.time.Instant

class CouponSource(
    val id: CouponSourceId?,
    val name: String,
    val discountAmount: BigDecimal,
    val createdAt: Instant,
    status: CouponSourceStatus,
    quantity: Int,
    updatedAt: Instant,
) {
    var status: CouponSourceStatus = status
        private set

    var quantity: Int = quantity

    var updatedAt: Instant = updatedAt
        private set

    fun requireId(): CouponSourceId =
        id ?: throw RequiredCouponSourceIdException()

}
