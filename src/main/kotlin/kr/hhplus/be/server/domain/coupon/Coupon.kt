package kr.hhplus.be.server.domain.coupon

import jakarta.persistence.*
import kr.hhplus.be.server.domain.coupon.exception.AlreadyUsedCouponException
import kr.hhplus.be.server.domain.coupon.exception.NotOwnedCouponException
import kr.hhplus.be.server.domain.coupon.exception.RequiredCouponIdException
import kr.hhplus.be.server.domain.coupon.result.UsedCoupon
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

@Entity
@Table(
    name = "coupons",
    indexes = [
        Index(name = "coupon_idx_user", columnList = "user_id"),
    ]
)
class Coupon(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null,
    @Column(nullable = false)
    val userId: UserId,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val couponSourceId: CouponSourceId,
    @Column(precision = 20, scale = 2)
    val discountAmount: BigDecimal,
    @Column(nullable = false)
    val createdAt: Instant,
    usedAt: Instant?,
    updatedAt: Instant,
) {
    @Column
    var usedAt: Instant? = usedAt
        private set

    @Column(nullable = false)
    var updatedAt: Instant = updatedAt
        private set

    fun id(): CouponId =
        id?.let { CouponId(it) } ?: throw RequiredCouponIdException()

    fun use(userId: UserId): UsedCoupon {
        if (this.userId != userId) {
            throw NotOwnedCouponException(id = id(), ownerId = this.userId, userId = userId)
        }
        if (usedAt != null) {
            throw AlreadyUsedCouponException(id = id())
        }
        val usedAt = Instant.now()
        this.usedAt = usedAt
        this.updatedAt = usedAt
        return UsedCoupon(
            id = id(),
            userId = userId,
            discountAmount = discountAmount,
            usedAt = usedAt,
        )
    }

    fun cancelUse() {
        if (usedAt == null) return

        usedAt = null
        updatedAt = Instant.now()
    }

    fun calculateDiscountAmount(totalAmount: BigDecimal): BigDecimal =
        totalAmount.min(discountAmount)

    companion object {
        fun new(
            userId: UserId,
            couponSourceId: CouponSourceId,
            name: String,
            discountAmount: BigDecimal,
            createdAt: Instant,
        ): Coupon = Coupon(
            id = null,
            userId = userId,
            name = name,
            couponSourceId = couponSourceId,
            discountAmount = discountAmount.setScale(2, RoundingMode.HALF_UP),
            usedAt = null,
            createdAt = createdAt,
            updatedAt = createdAt,
        )
    }
}
