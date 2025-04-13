package kr.hhplus.be.server.domain.coupon.exception

import kr.hhplus.be.server.domain.DomainException
import kr.hhplus.be.server.domain.coupon.CouponId

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
