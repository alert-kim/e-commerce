package kr.hhplus.be.server.domain.coupon

import jakarta.persistence.Embeddable

@Embeddable
data class CouponId(val value: Long)
