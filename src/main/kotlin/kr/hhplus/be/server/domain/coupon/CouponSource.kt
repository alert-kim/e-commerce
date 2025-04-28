package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.coupon.exception.OutOfStockCouponSourceException
import kr.hhplus.be.server.domain.coupon.exception.RequiredCouponSourceIdException
import kr.hhplus.be.server.domain.coupon.result.IssuedCoupon
import java.math.BigDecimal
import java.time.Instant

class CouponSource(
    val id: CouponSourceId?,
    val name: String,
    val discountAmount: BigDecimal,
    val initialQuantity: Int,
    val createdAt: Instant,
    status: CouponSourceStatus,
    quantity: Int,
    updatedAt: Instant,
) {
    init {
        require(quantity >= 0) { "쿠폰 재고는 0 이상이어야 합니다." }
    }

    var status: CouponSourceStatus = status
        private set

    var quantity: Int = quantity

    var updatedAt: Instant = updatedAt
        private set

    fun issue(): IssuedCoupon {
        if (status == CouponSourceStatus.OUT_OF_STOCK) {
            throw OutOfStockCouponSourceException(
                sourceId = requireId(),
                required = ISSUE_COUNT,
                remaining = quantity,
            )
        }
        quantity -= ISSUE_COUNT
        updatedAt = Instant.now()

        if (quantity == 0) {
            status = CouponSourceStatus.OUT_OF_STOCK
        }

        return IssuedCoupon(
            couponSourceId = requireId(),
            name = name,
            discountAmount = discountAmount,
            createdAt = Instant.now(),
        )
    }

    fun requireId(): CouponSourceId =
        id ?: throw RequiredCouponSourceIdException()

    companion object {
        private const val ISSUE_COUNT = 1
    }
}
