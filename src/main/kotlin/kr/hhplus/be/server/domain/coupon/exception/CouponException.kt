package kr.hhplus.be.server.domain.coupon.exception

import kr.hhplus.be.server.domain.DomainException
import kr.hhplus.be.server.domain.coupon.CouponId
import kr.hhplus.be.server.domain.user.UserId

abstract class CouponException : DomainException()

class RequiredCouponIdException(
) : CouponException() {
    override val message: String = "쿠폰 ID가 필요합니다."
}

class NotFoundCouponException(
    detail: String,
) : CouponException() {
    override val message: String = "쿠폰을 찾을 수 없습니다. $detail"
}

class AlreadyUsedCouponException(
    id: CouponId,
) : CouponException() {
    override val message: String = "이미 사용된 쿠폰(${id.value})입니다"
}

class ExpiredCouponException(
    id: CouponId,
) : CouponException() {
    override val message: String = "쿠폰(${id.value})이 만료되었습니다"
}

class NotOwnedCouponException(
    id: CouponId,
    ownerId: UserId,
    userId: UserId,
) : CouponException() {
    override val message: String = "쿠폰(${id.value})을 소유하고 있지 않습니다. 소유자: ${ownerId.value}, 요청자: ${userId.value}"
}
