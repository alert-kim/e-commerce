package kr.hhplus.be.server.domain.coupon

import org.springframework.stereotype.Service

@Service
class CouponSourceService(
    private val repository: CouponSourceRepository,
) {
    fun getAllIssuable(): List<CouponSource> =
        repository.findAllByStatus(CouponSourceStatus.ACTIVE)
}
