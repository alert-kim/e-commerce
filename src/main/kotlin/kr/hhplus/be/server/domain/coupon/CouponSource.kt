package kr.hhplus.be.server.domain.coupon

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.hhplus.be.server.domain.coupon.exception.OutOfStockCouponSourceException
import kr.hhplus.be.server.domain.coupon.exception.RequiredCouponSourceIdException
import kr.hhplus.be.server.domain.coupon.result.IssuedCoupon
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "coupon_sources")
class CouponSource(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected val id: Long? = null,
    val name: String,
    @Column(precision = 20, scale = 2)
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

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)")
    var status: CouponSourceStatus = status
        private set

    var quantity: Int = quantity
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun issue(): IssuedCoupon {
        if (status == CouponSourceStatus.OUT_OF_STOCK) {
            throw OutOfStockCouponSourceException(
                sourceId = id(),
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
            couponSourceId = id(),
            name = name,
            discountAmount = discountAmount,
            createdAt = Instant.now(),
        )
    }

    fun id(): CouponSourceId =
        id?.let { CouponSourceId(it) } ?: throw RequiredCouponSourceIdException()

    companion object {
        private const val ISSUE_COUNT = 1
    }
}
