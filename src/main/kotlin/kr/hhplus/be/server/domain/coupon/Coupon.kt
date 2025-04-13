package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.coupon.exception.AlreadyUsedCouponException
import kr.hhplus.be.server.domain.coupon.exception.NotOwnedCouponException
import kr.hhplus.be.server.domain.coupon.exception.RequiredCouponIdException
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

class Coupon(
    val id: CouponId?,
    val userId: UserId,
    val name: String,
    val couponSourceId: CouponSourceId,
    val discountAmount: BigDecimal,
    val createdAt: Instant,
    usedAt: Instant?,
    updatedAt: Instant,
) {
    var usedAt: Instant? = usedAt
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun requireId(): CouponId =
        id ?: throw RequiredCouponIdException()

    fun use(userId: UserId) {
        if (this.userId != userId) {
            throw NotOwnedCouponException(id = requireId(), ownerId = this.userId, userId = userId)
        }
        if (usedAt != null) {
            throw AlreadyUsedCouponException(id = requireId())
        }
        usedAt = Instant.now()
    }
}
